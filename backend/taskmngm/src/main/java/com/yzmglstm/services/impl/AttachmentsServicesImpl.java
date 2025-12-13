package com.yzmglstm.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import com.yzmglstm.dto.DtoAttachment;
import com.yzmglstm.dto.DtoAttachments;
import com.yzmglstm.entities.Attachments;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.AttachmentsRepository;
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IAttachmentsServices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentsServicesImpl implements IAttachmentsServices {

    private final AttachmentsRepository attachmentsRepository;
    private final TasksRepository tasksRepository;
    private final UsersRepository usersRepository;
    
    // Dosyaların kaydedileceği klasör (application.properties'den de çekebilirsin)
    private final String UPLOAD_DIR = "uploads/";

    // İzin verilen uzantılar
    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "png", "jpg", "docx", "xlsx");

    @Autowired
    public AttachmentsServicesImpl(AttachmentsRepository ar, TasksRepository tr, UsersRepository ur) {
        this.attachmentsRepository = ar;
        this.tasksRepository = tr;
        this.usersRepository = ur;
        
        // Klasör yoksa oluştur
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private Users getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByMail(email).orElseThrow();
    }

    @Override
    @Transactional
    public DtoAttachments uploadAttachment(Long taskId, MultipartFile file) {
        // 1. Dosya Kontrolleri
        if (file.isEmpty()) throw new RuntimeException("Dosya boş olamaz.");
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("Desteklenmeyen dosya formatı: " + extension);
        }

        // Boyut kontrolü (Spring Boot properties ile de yapılır ama burada manuel de eklenebilir)
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new RuntimeException("Dosya boyutu 10MB'dan büyük olamaz.");
        }

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Görev bulunamadı."));

        // 2. Dosyayı Diske Kaydet (İsim çakışmasını önlemek için UUID ekle)
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);

        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Dosya yüklenirken hata oluştu.", e);
        }

        // 3. Veritabanına Kaydet
        Attachments attachment = new Attachments();
        attachment.setOriginalFilename(originalFilename);
        attachment.setStoragePath(filePath.toString());
        attachment.setFileType(extension);
        attachment.setFileSize(file.getSize());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setTask(task);
        attachment.setUploader(getLoggedInUser());

        Attachments saved = attachmentsRepository.save(attachment);

        // 4. DTO Dönüşümü
        DtoAttachments dto = new DtoAttachments();
        dto.setId(saved.getId());
        dto.setOriginalFilename(saved.getOriginalFilename());
        dto.setFileSize(saved.getFileSize());
        dto.setUploadDate(saved.getUploadDate());
        dto.setUrl("/api/attachments/download/" + saved.getId()); // İndirme linki
        
        return dto;
    }

    @Override
    public List<DtoAttachments> getAttachmentsByTask(Long taskId) {
        List<Attachments> list = attachmentsRepository.findByTaskId(taskId);
        List<DtoAttachments> dtos = new ArrayList<>();
        
        for (Attachments att : list) {
            DtoAttachments dto = new DtoAttachments();
            dto.setId(att.getId());
            dto.setOriginalFilename(att.getOriginalFilename());
            dto.setFileSize(att.getFileSize());
            dto.setUploadDate(att.getUploadDate());
            dto.setUrl("/api/attachments/download/" + att.getId());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public byte[] getAttachmentData(Long attachmentId) {
        Attachments att = attachmentsRepository.findById(attachmentId).orElseThrow();
        try {
            return Files.readAllBytes(Paths.get(att.getStoragePath()));
        } catch (IOException e) {
            throw new RuntimeException("Dosya okunamadı.");
        }
    }
    
    @Override
    public String getAttachmentName(Long attachmentId) {
        return attachmentsRepository.findById(attachmentId).orElseThrow().getOriginalFilename();
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachments att = attachmentsRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Dosya bulunamadı."));
        
        // Önce diskten sil
        try {
            Files.deleteIfExists(Paths.get(att.getStoragePath()));
        } catch (IOException e) {
           // Log basılabilir
        }
        
        // Sonra DB'den sil
        attachmentsRepository.delete(att);
    }
}