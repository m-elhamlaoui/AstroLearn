package com.example.demo.repository;

import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;


import com.example.demo.model.User.*;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByLevel(User.UserLevel level);

    List<User> findByVerificationStatus(User.UserVerification status);

}
