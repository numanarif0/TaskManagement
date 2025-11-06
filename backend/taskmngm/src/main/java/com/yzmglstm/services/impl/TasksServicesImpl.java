package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder; // <-- YENİ IMPORT
import org.springframework.security.core.Authentication; // <-- YENİ IMPORT
import org.springframework.stereotype.Service;

import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.entities.Users; // <-- YENİ IMPORT
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.repository.UsersRepository; // <-- YENİ IMPORT
import com.yzmglstm.services.ITasksServices;

import java.util.ArrayList; // <-- YENİ IMPORT
import java.util.List; // <-- YENİ IMPORT

@Service
public class TasksServicesImpl implements ITasksServices {

    // --- YENİ EKLENEN BAĞIMLILIKLAR ---
    private final TasksRepository tasksRepository;
    private final UsersRepository usersRepository; // Kullanıcıyı e-postasından bulmak için eklendi

    @Autowired
    public TasksServicesImpl(TasksRepository tasksRepository, UsersRepository usersRepository) {
        this.tasksRepository = tasksRepository;
        this.usersRepository = usersRepository;
    }

    // --- YARDIMCI METOT ---
    // Spring Security'den o an giriş yapmış kullanıcıyı bulan özel bir metot.
    private Users getLoggedInUser() {
        // O anki giriş kimlik bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Kullanıcının 'username' (bizim projemizde bu e-postadır) bilgisini al
        String userEmail = authentication.getName();
        
        // E-posta'yı kullanarak UsersRepository'den tam kullanıcı nesnesini bul ve döndür
        return usersRepository.findByMail(userEmail)
                .orElseThrow(() -> new RuntimeException("Giriş yapmış kullanıcı veritabanında bulunamadı: " + userEmail));
    }

    // --- GÜNCELLENEN METOT ---
    @Override
    public DtoTask saveTask(DtoTaskIU dtoTasks) {

        // 1. Adım: Giriş yapmış kullanıcıyı bul.
        Users loggedInUser = getLoggedInUser();

        // 2. Adım: DTO'dan gelen verileri kopyala.
        Tasks tasks = new Tasks();
        BeanUtils.copyProperties(dtoTasks, tasks);

        // 3. Adım: KRİTİK! Görevin sahibini (user) ata.
        tasks.setUser(loggedInUser);

        // 4. Adım: Veritabanına kaydet.
        Tasks saveTasks = tasksRepository.save(tasks);
        
        // 5. Adım: Cevabı döndür.
        DtoTask response = new DtoTask();
        BeanUtils.copyProperties(saveTasks, response);
        return response;
    }

    // --- YENİ EKLENEN METOT (PDF'teki GET /api/tasks için) ---
    @Override
    public List<DtoTask> GetAllTasks() {
        
        // 1. Adım: Giriş yapmış kullanıcıyı ve ID'sini bul.
        Users loggedInUser = getLoggedInUser();
        Long userId = loggedInUser.getId();

        // 2. Adım: Kiler'e (Repository) git ve Adım 2'de yazdığımız "sihirli" metodu kullan.
        // Bu, SADECE bu kullanıcının görevlerini getirir (SELECT * ... WHERE user_id = ?)
        List<Tasks> userTasks = tasksRepository.findByUserId(userId);

        // 3. Adım: Veritabanı nesnelerini (Tasks) cevap nesnelerine (DtoTask) dönüştür.
        List<DtoTask> dtoList = new ArrayList<>();
        for (Tasks task : userTasks) {
            DtoTask dto = new DtoTask();
            BeanUtils.copyProperties(task, dto);
            dtoList.add(dto);
        }

        // 4. Adım: Listeyi döndür.
        return dtoList;
    }
}