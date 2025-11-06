package com.yzmglstm.controller.impl;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.GetMapping; // <-- YENİ IMPORT
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yzmglstm.controller.ITasksController;
import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import com.yzmglstm.services.ITasksServices;

import java.util.List; // <-- YENİ IMPORT

@RestController
@RequestMapping("/api/tasks") 
public class TasksControllerImpl implements ITasksController {

    @Autowired 
    private ITasksServices tasksServices;

    // Bu, PDF'teki "POST /api/tasks" (Add new task) API'sidir
    @Override
    @PostMapping 
    public DtoTask saveTasks(@RequestBody DtoTaskIU dtoTasks) { 
        // Mutfak'taki (Service) 'saveTask' metodunu çağırır.
        // Bu metodu az önce kullanıcıyı atayacak şekilde güncellemiştik.
        return tasksServices.saveTask(dtoTasks);
    }
    
    // --- YENİ EKLENEN METOT ---
    // Bu, PDF'teki "GET /api/tasks" (List user's tasks) API'sidir
    @Override
    @GetMapping // GET isteklerini bu adresten dinler
    public List<DtoTask> GetAllTasks() {
        // Mutfak'taki (Service) 'GetAllTasks' metodunu çağırır.
        // Bu metodu az önce SADECE giriş yapan kullanıcının görevlerini
        // getirecek şekilde güncellemiştik.
        return tasksServices.GetAllTasks();
    }
    // -------------------------
}