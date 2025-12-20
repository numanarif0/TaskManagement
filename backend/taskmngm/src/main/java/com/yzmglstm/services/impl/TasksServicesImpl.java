package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yzmglstm.dto.DtoAttachments;
import com.yzmglstm.dto.DtoFilterTasks;
import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IAttachmentsServices;
import com.yzmglstm.services.ITasksServices;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TasksServicesImpl implements ITasksServices {

    private final TasksRepository tasksRepository;
    private final UsersRepository usersRepository;
    private final IAttachmentsServices attachmentsServices; // PDF 8.1: Dosya temizliği için eklendi 

    @Autowired
    public TasksServicesImpl(TasksRepository tasksRepository, 
                             UsersRepository usersRepository,
                             IAttachmentsServices attachmentsServices) {
        this.tasksRepository = tasksRepository;
        this.usersRepository = usersRepository;
        this.attachmentsServices = attachmentsServices;
    }

    // --- YARDIMCI METOT (Giriş Yapan Kullanıcıyı Bul) ---
    private Users getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usersRepository.findByMail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userEmail));
    }
    
    // --- YARDIMCI METOT (Admin Kontrolü) ---
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN")); // SecurityConfig ile uyumlu 
    }

    // --- YARDIMCI METOT (Güvenlik Kontrolü) ---
    private Tasks checkTaskOwnerAndGet(Long taskId) {
        Users loggedInUser = getLoggedInUser();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Görev bulunamadı: " + taskId));

        // PDF 8.2: Admin değilse ve sahibi değilse erişimi engelle [cite: 72, 80, 83]
        if (!isAdmin(auth) && !task.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("Yetkisiz erişim: Bu görev üzerinde işlem yapma izniniz yok.");
        }
        
        return task;
    }

    // --- GÖREV EKLEME (POST) ---
    @Override
    public DtoTask saveTask(DtoTaskIU dtoTasks) {
        Users loggedInUser = getLoggedInUser();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Users targetUser = loggedInUser; 

        // PDF 8.2: Admin ise başkasına görev atayabilir [cite: 73, 81]
        if (isAdmin(auth) && dtoTasks.getAssignedUserId() != null) {
            targetUser = usersRepository.findById(dtoTasks.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Atanacak kullanıcı bulunamadı."));
        }
        
        Tasks task = new Tasks();
        BeanUtils.copyProperties(dtoTasks, task);
        task.setUser(targetUser);
        
        Tasks savedTask = tasksRepository.save(task);
        DtoTask response = new DtoTask();
        BeanUtils.copyProperties(savedTask, response);
        return response;
    }

    // --- GÖREV LİSTELEME (GET) ---
    @Override
    public List<DtoTask> GetAllTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users loggedInUser = getLoggedInUser();

        List<Tasks> tasksList;
        
        // PDF 8.2: Admin tümünü, User sadece kendininkini görür [cite: 72, 73]
        if (isAdmin(auth)) {
            tasksList = tasksRepository.findAll();
        } else {
            tasksList = tasksRepository.findByUserId(loggedInUser.getId());
        }
        
        List<DtoTask> dtoList = new ArrayList<>();
        for (Tasks task : tasksList) {
            DtoTask dto = new DtoTask();
            BeanUtils.copyProperties(task, dto);
            dtoList.add(dto);
        }
        return dtoList;
    }

    // --- GÖREV DÜZENLEME (PUT) ---
    @Override
    public DtoTask updateTask(Long taskId, DtoTaskIU dtoTasks) {
        Tasks existingTask = checkTaskOwnerAndGet(taskId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        BeanUtils.copyProperties(dtoTasks, existingTask, "id", "user");

        // Admin ise görevin sahibini değiştirebilir 
        if (isAdmin(auth) && dtoTasks.getAssignedUserId() != null) {
             Users newUser = usersRepository.findById(dtoTasks.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
             existingTask.setUser(newUser);
        }
        
        Tasks updatedTask = tasksRepository.save(existingTask);
        DtoTask response = new DtoTask();
        BeanUtils.copyProperties(updatedTask, response);
        return response;
    }

    // --- GÖREV SİLME (DELETE) ---
    @Override
    public void deleteTask(Long taskId) {
        Tasks taskToDelete = checkTaskOwnerAndGet(taskId);
        
        // PDF 8.1: Görev silindiğinde tüm dosyaları da (fiziksel olarak) sil 
        List<DtoAttachments> attachments = attachmentsServices.getAttachmentsByTask(taskId);
        for (DtoAttachments att : attachments) {
            attachmentsServices.deleteAttachment(att.getId());
        }
        
        tasksRepository.delete(taskToDelete);
    }

    // --- İSTATİSTİKLER (GET /api/tasks/stats) ---
    @Override
    public DtoFilterTasks filterGetTasks() {
        DtoFilterTasks dto = new DtoFilterTasks();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // PDF 6: İstatistiklerin rol bazlı hesaplanması [cite: 24, 30, 34]
        if (isAdmin(auth)) {
            dto.setTotalTasks(tasksRepository.count());
            dto.setCompletedTasks(tasksRepository.countByStatus("COMPLETED"));
            dto.setPendingTasks(tasksRepository.countByStatus("PENDING"));
            dto.setInProgressTasks(tasksRepository.countByStatus("IN_PROGRESS"));
        } else {
            Long userId = getLoggedInUser().getId();
            dto.setTotalTasks(tasksRepository.countByUserId(userId));
            dto.setCompletedTasks(tasksRepository.countByStatusAndUserId("COMPLETED", userId));
            dto.setPendingTasks(tasksRepository.countByStatusAndUserId("PENDING", userId));
            dto.setInProgressTasks(tasksRepository.countByStatusAndUserId("IN_PROGRESS", userId));
        }
        return dto;
    }
}