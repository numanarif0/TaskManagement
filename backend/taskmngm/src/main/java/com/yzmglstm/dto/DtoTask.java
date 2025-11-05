package com.yzmglstm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoTask {

    
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private Date dueDate;
    private Date dueTime;

}