package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- YENİ IMPORT

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
    // 1. Giriş yapan kullanıcıyı al
    Users loggedInUser = getLoggedInUser();
    
    // 2. Yeni görev oluştur
    Tasks tasks = new Tasks();
    BeanUtils.copyProperties(dtoTasks, tasks);
    
    // 3. KRITIK: Görevi kullanıcıya ata
    tasks.setUser(loggedInUser);
    
    // 4. Veritabanına kaydet
    Tasks saveTasks = tasksRepository.save(tasks);
    
    // 5. Response oluştur
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
        
        // 1. Güvenlik Kontrolü: Bu görev bu kullanıcıya mı ait?
        Tasks existingTask = checkTaskOwnerAndGet(taskId);
        
        // 2. Güncelleme: DTO'dan gelen yeni verileri mevcut göreve kopyala
        // (id ve user alanı hariç)
        BeanUtils.copyProperties(dtoTasks, existingTask, "id", "user");
        
        // 3. Kaydet: Değişiklikleri kaydet
        Tasks updatedTask = tasksRepository.save(existingTask);
        
        // 4. Cevapla
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
}