package com.yzmglstm.controller.impl;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yzmglstm.controller.ITasksController;
import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.services.ITasksServices;

@RestController
@RequestMapping("rest/api/tasks")
public class TasksControllerImpl implements ITasksController {

    @Autowired 
    private ITasksServices tasksServices;

    @Override
    @PostMapping 
    public DtoTask saveTasks(@RequestBody DtoTaskIU dtoTasks) { 
        return tasksServices.saveTask(dtoTasks);
    }
    
}