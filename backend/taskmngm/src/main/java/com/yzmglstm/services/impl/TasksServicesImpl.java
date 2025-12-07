package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- YENİ IMPORT

import com.yzmglstm.dto.DtoFilterTasks;
import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.ITasksServices;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional // Veritabanı işlemleri için bu notasyonu eklemek iyi bir pratiktir
public class TasksServicesImpl implements ITasksServices {

    private final TasksRepository tasksRepository;
    private final UsersRepository usersRepository;

    @Autowired
    public TasksServicesImpl(TasksRepository tasksRepository, UsersRepository usersRepository) {
        this.tasksRepository = tasksRepository;
        this.usersRepository = usersRepository;
    }

    // --- YARDIMCI METOT (Giriş Yapan Kullanıcıyı Bul) ---
    private Users getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return usersRepository.findByMail(userEmail)
                .orElseThrow(() -> new RuntimeException("Giriş yapmış kullanıcı veritabanında bulunamadı: " + userEmail));
    }
    
    // --- YARDIMCI METOT (Güvenlik Kontrolü) ---
    // Bu metot, bir görevin giriş yapan kullanıcıya ait olup olmadığını kontrol eder
    private Tasks checkTaskOwnerAndGet(Long taskId) {
    Users loggedInUser = getLoggedInUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Tasks task = tasksRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Görev bulunamadı: " + taskId));

    // KURAL: Eğer kullanıcı ADMIN değilse VE görevin sahibi değilse hata ver.
    // Yani: Admin ise geç, sahibi ise geç. İkisi de değilse durdur.
    if (!isAdmin(auth) && !task.getUser().getId().equals(loggedInUser.getId())) {
        throw new RuntimeException("Yetkisiz erişim: Bu işlemi yapmaya izniniz yok.");
    }
    
    return task;
    }


    // --- GÖREV EKLEME (POST) ---
    @Override
public DtoTask saveTask(DtoTaskIU dtoTasks) {
    Users loggedInUser = getLoggedInUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    // Varsayılan hedef: Görevi kendine ata
    Users targetUser = loggedInUser; 

    // KURAL: Admin ise VE "assignedUserId" gönderilmişse hedefi değiştir
    if (isAdmin(auth) && dtoTasks.getAssignedUserId() != null) {
        targetUser = usersRepository.findById(dtoTasks.getAssignedUserId())
                .orElseThrow(() -> new RuntimeException("Atanacak kullanıcı (ID: " + dtoTasks.getAssignedUserId() + ") bulunamadı!"));
    }
    
    Tasks tasks = new Tasks();
    BeanUtils.copyProperties(dtoTasks, tasks);
    
    tasks.setUser(targetUser); // Belirlenen kullanıcıyı set et
    
    Tasks saveTasks = tasksRepository.save(tasks);
    DtoTask response = new DtoTask();
    BeanUtils.copyProperties(saveTasks, response);
    return response;
}
    // --- GÖREV LİSTELEME (GET) ---
    @Override
    public List<DtoTask> GetAllTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users loggedInUser = getLoggedInUser();

        List<Tasks> tasksList;
        
        // Eğer Admin ise TÜM görevleri getir, değilse sadece KENDİ görevlerini getir
        if (isAdmin(auth)) {
            tasksList = tasksRepository.findAll(); // Admin hepsini görür
        } else {
            tasksList = tasksRepository.findByUserId(loggedInUser.getId()); // User sadece kendininkini
        }
        
        // ... DTO dönüşüm kodları aynı kalacak ...
        List<DtoTask> dtoList = new ArrayList<>();
        for (Tasks task : tasksList) {
            DtoTask dto = new DtoTask();
            BeanUtils.copyProperties(task, dto);
            dtoList.add(dto);
        }
        return dtoList;
    }

    // --- YENİ EKLENEN METOT: GÖREV DÜZENLEME (PUT /api/tasks/:id) ---
    @Override
public DtoTask updateTask(Long taskId, DtoTaskIU dtoTasks) {
    Tasks existingTask = checkTaskOwnerAndGet(taskId);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // 1. Standart alanları kopyala (id ve user hariç)
    BeanUtils.copyProperties(dtoTasks, existingTask, "id", "user");

    // 2. KURAL: Admin ise VE yeni bir sahip ID'si göndermişse sahibini değiştir
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

    // --- YENİ EKLENEN METOT: GÖREV SİLME (DELETE /api/tasks/:id) ---
    @Override
    public void deleteTask(Long taskId) {
        
        // 1. Güvenlik Kontrolü: Bu görev bu kullanıcıya mı ait?
        Tasks taskToDelete = checkTaskOwnerAndGet(taskId);
        
        // 2. Sil: Görevi sil
        tasksRepository.delete(taskToDelete);
        
        // (Bu metot bir şey döndürmez, HTTP 204 No Content beklenir)
    }

    private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().stream()
            .anyMatch(r -> r.getAuthority().equals("ADMIN"));
}

    @Override
    public DtoFilterTasks filterGetTasks() {
        DtoFilterTasks dtoFilterTasks = new DtoFilterTasks();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If admin -> return global stats, otherwise return stats only for the logged-in user
        if (isAdmin(auth)) {
            Long totalTasks = tasksRepository.count();
            Long completedTasks = tasksRepository.countByStatus("COMPLETED");
            Long pendingTasks = tasksRepository.countByStatus("PENDING");
            Long inProgressTasks = tasksRepository.countByStatus("IN_PROGRESS");

            dtoFilterTasks.setTotalTasks(totalTasks);
            dtoFilterTasks.setCompletedTasks(completedTasks);
            dtoFilterTasks.setPendingTasks(pendingTasks);
            dtoFilterTasks.setInProgressTasks(inProgressTasks);
        } else {
            Users loggedInUser = getLoggedInUser();
            Long userId = loggedInUser.getId();

            Long totalTasks = tasksRepository.countByUserId(userId);
            Long completedTasks = tasksRepository.countByStatusAndUserId("Completed", userId);
            Long pendingTasks = tasksRepository.countByStatusAndUserId("Pending", userId);
            Long inProgressTasks = tasksRepository.countByStatusAndUserId("In Progress", userId);

            dtoFilterTasks.setTotalTasks(totalTasks);
            dtoFilterTasks.setCompletedTasks(completedTasks);
            dtoFilterTasks.setPendingTasks(pendingTasks);
            dtoFilterTasks.setInProgressTasks(inProgressTasks);
        }

        return dtoFilterTasks;
    }

}