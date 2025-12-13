package com.yzmglstm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DtoAttachments {
    
    private Long id;
    private String originalFilename;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private String url; 

}