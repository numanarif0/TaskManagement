package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.yzmglstm.dto.DtoUsers;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IUsersServices;
import com.yzmglstm.dto.DtoUsersIU;
import com.yzmglstm.entities.Users;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersServicesImpl implements IUsersServices {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public DtoUsers saveUsers(DtoUsersIU dtoUsersIU) {
        
        DtoUsers response = new DtoUsers();
        Users user = new Users();   
        BeanUtils.copyProperties(dtoUsersIU, user);
        
        Users savedUser = usersRepository.save(user);

        BeanUtils.copyProperties(savedUser, response);


        return response;
      
    }


    @Override
    public List <DtoUsers> GetAllUsers(){

        List <DtoUsers> dtoResponseList = new ArrayList<>();
        List <Users> users = usersRepository.findAll();

        for(Users user : users){
            DtoUsers dtoUsers = new DtoUsers();
            BeanUtils.copyProperties(user, dtoUsers);
            dtoResponseList.add(dtoUsers);
        }

        return dtoResponseList;

    }

}
