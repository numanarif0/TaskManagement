package com.yzmglstm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.yzmglstm.entities.Users;
@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

}
