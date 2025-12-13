package com.yzmglstm.services;

import org.springframework.web.multipart.MultipartFile;
import com.yzmglstm.dto.DtoAttachments;

import java.util.List;

public interface IAttachmentsServices {
    DtoAttachments uploadAttachment(Long taskId, MultipartFile file);
    List<DtoAttachments> getAttachmentsByTask(Long taskId);
    byte[] getAttachmentData(Long attachmentId); // İndirmek için
    String getAttachmentName(Long attachmentId); // İndirme ismi için
    void deleteAttachment(Long attachmentId);
}