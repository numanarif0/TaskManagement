package com.yzmglstm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.yzmglstm.dto.DtoAttachments;

public interface IAttachmentsController {


    public DtoAttachments uploadAttachments(MultipartFile file, Long taskId);
    public List<DtoAttachments> getAttachmentsByTask(Long taskID);
    public void deleteAttachment(Long id);
    public ResponseEntity<Byte[]> downloadAttachment(Long id);
    


}
