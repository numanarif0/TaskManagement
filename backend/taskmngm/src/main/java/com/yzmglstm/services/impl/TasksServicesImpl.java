package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;

import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.entities.Tasks;
import com.yzmglstm.repository.TasksRepository;
import com.yzmglstm.services.ITasksServices;

@Service
public class TasksServicesImpl implements ITasksServices{

    @Autowired
    private TasksRepository tasksRepository  ;

        @Override
        public DtoTask saveTask(DtoTaskIU dtoTasks){

            Tasks tasks = new Tasks();
            BeanUtils.copyProperties(dtoTasks, tasks);

            Tasks saveTasks = tasksRepository.save(tasks);
            DtoTask response = new DtoTask();
            BeanUtils.copyProperties(saveTasks, response);
            return response;
        }
}
