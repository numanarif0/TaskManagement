package com.yzmglstm.services.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // <-- YENİ
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- YENİ

import com.yzmglstm.dto.DtoUsers;
import com.yzmglstm.dto.DtoUsersIU;
import com.yzmglstm.dto.DtoLoginRequest;

import com.yzmglstm.entities.Users;
import com.yzmglstm.repository.UsersRepository;
import com.yzmglstm.services.IUsersServices;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // <-- YENİ
import java.util.Optional;

@Service
public class UsersServicesImpl implements IUsersServices {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServicesImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Yardımcılar (normalize) ----
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String raw) {
        return raw == null ? null : raw.replaceAll("[^0-9]", ""); // sadece rakam
    }

    // ============= REGISTER =============
    @Override
    @Transactional
    public DtoUsers saveUsers(DtoUsersIU dtoUsersIU) {

        // 1) Normalize
        String mail  = normalizeEmail(dtoUsersIU.getMail());
        String phone = normalizePhone(dtoUsersIU.getPhone());

        // 2) Ön-kontrol (hızlı feedback)
        if (mail != null && usersRepository.existsByMailIgnoreCase(mail)) {
            throw new RuntimeException("Bu e-posta zaten kayıtlı.");
        }
        if (phone != null && usersRepository.existsByPhone(phone)) {
            throw new RuntimeException("Bu telefon zaten kayıtlı.");
        }

        // 3) Kopyala + normalize değerleri zorla
        Users user = new Users();
        BeanUtils.copyProperties(dtoUsersIU, user);
        user.setMail(mail);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(dtoUsersIU.getPassword()));

        try {
            Users savedUser = usersRepository.save(user);
            DtoUsers response = new DtoUsers();
            BeanUtils.copyProperties(savedUser, response);
            return response;
        } catch (DataIntegrityViolationException ex) {
            // Aynı anda iki istek gelirse DB'deki UNIQUE constraint patlayabilir
            throw new RuntimeException("E-posta veya telefon zaten kayıtlı.", ex);
        }
    }

    // ============= LIST =============
    @Override
    public List<DtoUsers> GetAllUsers() {
        List<DtoUsers> dtoResponseList = new ArrayList<>();
        List<Users> users = usersRepository.findAll();
        for (Users user : users) {
            DtoUsers dtoUsers = new DtoUsers();
            BeanUtils.copyProperties(user, dtoUsers);
            dtoResponseList.add(dtoUsers);
        }
        return dtoResponseList;
    }

    // ============= LOGIN =============
    @Override
    public DtoUsers loginUser(DtoLoginRequest dtoLoginRequest) {
        Optional<Users> userOptional = usersRepository.findByMail(dtoLoginRequest.getMail());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("E-posta veya şifre hatalı.");
        }

        Users user = userOptional.get();
        String plainPassword = dtoLoginRequest.getPassword();
        String hashedPassword = user.getPassword();

        if (passwordEncoder.matches(plainPassword, hashedPassword)) {
            DtoUsers response = new DtoUsers();
            BeanUtils.copyProperties(user, response);
            return response;
        } else {
            throw new RuntimeException("E-posta veya şifre hatalı.");
        }
    }
}
