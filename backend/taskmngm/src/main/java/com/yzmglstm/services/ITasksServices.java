package com.yzmglstm.services;

import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;
import java.util.List; // <-- YENİ IMPORT

public interface ITasksServices {

    DtoTask saveTask(DtoTaskIU dtoTasks);
    
    // --- YENİ EKLENEN METOT İMZASI ---
    List<DtoTask> GetAllTasks();

}