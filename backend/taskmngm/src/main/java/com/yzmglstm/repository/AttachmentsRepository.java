package com.yzmglstm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yzmglstm.entities.Attachments;

public interface AttachmentsRepository extends JpaRepository<Attachments, Long>{


    List<Attachments> findByTaskId(Long taskId);

}


