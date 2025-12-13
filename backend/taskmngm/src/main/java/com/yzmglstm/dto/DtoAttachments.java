package com.yzmglstm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoAttachments {
    
    private Long id;
    private String fileName;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private String storagePath; 

}