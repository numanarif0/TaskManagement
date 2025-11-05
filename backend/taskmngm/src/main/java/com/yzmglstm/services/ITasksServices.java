package com.yzmglstm.services;

import com.yzmglstm.dto.DtoTask;
import com.yzmglstm.dto.DtoTaskIU;

public interface ITasksServices {


    public DtoTask saveTask(DtoTaskIU dtoTasks);

}
