package com.yzmglstm.services.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.yzmglstm.dto.DtoAttachments;
import com.yzmglstm.entities.Attachments;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.AttachmentsRepository;
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IAttachmentsServices;

import jakarta.transaction.Transactional;

@Service
public class AttachmentsServicesImpl implements IAttachmentsServices {

private final AttachmentsRepository attachmentsRepository;
private final TasksRepository tasksRepository;
private final UsersRepository usersRepository;

private final String UPLOAD_DIR = "uploads/";
private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "png", "jpg", "docx", "xlsx");


@Autowired
public AttachmentsServicesImpl(AttachmentsRepository attachmentsRepository, TasksRepository tasksRepository, UsersRepository usersRepository){
    this.attachmentsRepository = attachmentsRepository;
    this.tasksRepository = tasksRepository;
    this.usersRepository = usersRepository;

    File uploaDir = new File(UPLOAD_DIR);
    if(!uploaDir.exists()){
        uploaDir.mkdirs();
    }
}

private Users getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByMail(email).orElseThrow();
    }

@Override
@Transactional
public DtoAttachments uploadAttachments(MultipartFile file , Long taskId){
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
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Görev bulunamadı: " + taskId));
        
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage());
        }

        Attachments attachments = new Attachments();
        attachments.setFileName(uniqueFileName);
        attachments.setStoragePath(filePath.toString());
        attachments.setFileSize(file.getSize());
        attachments.setUploadDate(LocalDate.now());
        attachments.setFileType(extension);
        attachments.setOriginalFileName(originalFilename);
        attachments.setTask(task);
        attachments.setUser(getLoggedInUser());


        Attachments savedAttachment = attachmentsRepository.save(attachments);

        DtoAttachments response = new DtoAttachments();
        response.setId(savedAttachment.getId());
        response.setFileName(savedAttachment.getFileName());
        response.setOriginalFileName(savedAttachment.getOriginalFileName());
        response.setFileSize(savedAttachment.getFileSize());
        response.setUploadDate(LocalDateTime.now());
        response.setStoragePath("/api/attachments/download/" + savedAttachment.getId());
        return response;

}

@Override
public List<DtoAttachments> getAttachmentsByTask(Long taskId){

    List<Attachments> attachmentsList = attachmentsRepository.findByTaskId(taskId);
    List<DtoAttachments> dtoList = new ArrayList<>();
    for(Attachments att : attachmentsList){
        DtoAttachments dto = new DtoAttachments();
        dto.setId(att.getId());
        dto.setFileName(att.getFileName());
        dto.setOriginalFileName(att.getOriginalFileName());
        dto.setFileSize(att.getFileSize());
        dto.setUploadDate(att.getUploadDate().atStartOfDay());
        dto.setStoragePath("/api/attachments/download/" + att.getId());
        dtoList.add(dto);
    }
    return dtoList;
}

@Override
@Transactional
public void deleteAttachment(Long id) {
    Attachments attachment = attachmentsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ek bulunamadı: " + id));

    // PDF 8.1: Dosyayı sadece DB'den değil, klasörden de sil
    try {
        Path filePath = Paths.get(attachment.getStoragePath());
        Files.deleteIfExists(filePath);
    } catch (IOException e) {
        throw new RuntimeException("Fiziksel dosya silinemedi: " + e.getMessage());
    }

    attachmentsRepository.deleteById(id);
}
@Override
public ResponseEntity<byte[]> downloadAttachment(Long id){
    
    Attachments attachment = attachmentsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dosya bulunamadı: " + id));

    Path path = Paths.get(attachment.getStoragePath());

    try{

        byte[] data = Files.readAllBytes(path);

        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + attachment.getOriginalFileName() + "\"").contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(data);



    }catch(IOException e){
        throw new RuntimeException("Dosya indirme hatası: " + e.getMessage());
    }




   

}
}