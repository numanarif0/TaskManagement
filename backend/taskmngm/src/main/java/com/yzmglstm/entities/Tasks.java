package com.yzmglstm.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tasks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title",nullable = false,length = 100)
    private String title;

    @Column(name="description",nullable = false,length = 3000)
    private String description;

    @Column(name="category",nullable = false,length = 100)
    private String category;

    @Column(name="status",nullable = false)
    private String status;

    @Column(name="dueDate")
    private LocalDate dueDate; 

    @Column(name="dueTime")
    private LocalTime dueTime;



}
