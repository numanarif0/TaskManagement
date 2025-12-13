package com.yzmglstm.entities;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="attachments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attachments {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ; 

    @Column(name="original_file_name", nullable = false)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name= "file_type", nullable = false)
    private String fileType;

    @Column(name="file_size", nullable = false)
    private Long fileSize;

    @Column(name= "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="task_id", nullable = false)
    private Tasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="uploader_id", nullable = false)
    private Users uploader;




}
