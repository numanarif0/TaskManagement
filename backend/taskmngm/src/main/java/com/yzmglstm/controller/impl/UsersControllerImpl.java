package com.yzmglstm.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yzmglstm.dto.DtoUsersIU;
import com.yzmglstm.controller.IUsersController;
import com.yzmglstm.dto.DtoUsers;
import com.yzmglstm.services.IUsersServices;
import java.util.List;

import jakarta.validation.Valid;


@RestController
@RequestMapping("rest/api/users")
public class UsersControllerImpl implements IUsersController{

    @Autowired
    private IUsersServices userServices;
    
    @Override
    @PostMapping(path="/save")
    public DtoUsers saveUsers(@RequestBody @Valid DtoUsersIU dtoUsers) {
        return userServices.saveUsers(dtoUsers);
    }


    @GetMapping(path = "/get")
    @Override
    public List <DtoUsers> GetAllUsers(){
        return userServices.GetAllUsers();

    }

    




}
