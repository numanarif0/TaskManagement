package com.yzmglstm.controller;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import com.yzmglstm.dto.DtoAttachments;
import java.util.List;

public interface IAttachmentsController {

    DtoAttachments uploadAttachment(Long taskId, MultipartFile file);
    
    List<DtoAttachments> getAttachmentsByTask(Long taskId);
    
    ResponseEntity<byte[]> downloadAttachment(Long attachmentId);
    
    ResponseEntity<Void> deleteAttachment(Long attachmentId);
}
