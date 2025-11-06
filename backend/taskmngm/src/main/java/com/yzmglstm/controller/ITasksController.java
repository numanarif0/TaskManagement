package com.yzmglstm.controller;

import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import java.util.List; // <-- YENİ IMPORT

public interface ITasksController {

    DtoTask saveTasks(DtoTaskIU dtoTasks);
    
    // --- YENİ EKLENEN METOT İMZASI ---
    // Az önce Impl sınıfına eklediğimiz metodun imzası.
    List<DtoTask> GetAllTasks();

}