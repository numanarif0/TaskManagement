package com.yzmglstm.controller.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yzmglstm.controller.IAttachmentsController;
import com.yzmglstm.dto.DtoAttachments;




@RestController
@RequestMapping("/api/attachments")
public class AttachmentsControllerImpl implements IAttachmentsController {

@Autowired    
private IAttachmentsServices attachmentsServices;

@Override
@PostMapping("/upload/{taskId}")    
public DtoAttachments uploadAttachments(@RequestParam("file")MultipartFile file, @PathVariable Long taskId){
    return attachmentsServices.uploadAttachment(file, taskId);
}

@Override
@GetMapping("/task/{taskID}")
public List<DtoAttachments> getAttachmentsByTask(@PathVariable Long taskID){
    return attachmentsServices.getAttachmentsByTask(taskID);
}
@Override
@DeleteMapping("/{id}")
public void deleteAttachment(@PathVariable Long id){
   
}

@Override
@GetMapping("/download/{id}")
public ResponseEntity<Byte[]> downloadAttachment(@PathVariable Long id){

    return attachmentsServices.downloadAttachment(id);
}




}
