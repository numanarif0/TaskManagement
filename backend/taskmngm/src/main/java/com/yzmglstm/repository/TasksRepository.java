package com.yzmglstm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yzmglstm.entities.Tasks;



public interface TasksRepository extends JpaRepository<Tasks,Long>{


}
