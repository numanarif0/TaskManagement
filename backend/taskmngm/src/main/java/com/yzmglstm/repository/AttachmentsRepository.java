package com.yzmglstm.repository;

import org.springframework.stereotype.Repository;
import com.yzmglstm.entities.Attachments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface AttachmentsRepository extends JpaRepository<Attachments, Long> {

    List<Attachments> findByTaskId(Long taskId);

}
