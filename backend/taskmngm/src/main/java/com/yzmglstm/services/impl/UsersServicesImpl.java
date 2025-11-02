package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yzmglstm.dto.DtoUsers;
import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IUsersServices;
import com.yzmglstm.dto.DtoUsersIU;

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

}
