package com.example.Backend.Repository;

import com.example.Backend.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByUserName(String username);

    UserEntity findByEmail(String email);


}
