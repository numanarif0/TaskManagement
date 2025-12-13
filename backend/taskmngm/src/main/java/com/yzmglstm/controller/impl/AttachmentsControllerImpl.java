package com.yzmglstm.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.yzmglstm.controller.IAttachmentsController;
import com.yzmglstm.dto.DtoAttachments;
import com.yzmglstm.services.IAttachmentsServices;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentsControllerImpl implements IAttachmentsController {

    @Autowired
    private IAttachmentsServices attachmentServices;

    @Override
    @PostMapping("/upload/{taskId}")
    public DtoAttachments uploadAttachment(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) {
        return attachmentServices.uploadAttachment(taskId, file);
    }

    @Override
    @GetMapping("/task/{taskId}")
    public List<DtoAttachments> getAttachmentsByTask(@PathVariable Long taskId) {
        return attachmentServices.getAttachmentsByTask(taskId);
    }

    @Override
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        byte[] data = attachmentServices.getAttachmentData(id);
        String name = attachmentServices.getAttachmentName(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentServices.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}