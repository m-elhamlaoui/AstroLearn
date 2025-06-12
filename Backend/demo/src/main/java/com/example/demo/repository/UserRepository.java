package com.example.demo.repository;

import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;


import com.example.demo.model.User.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    //findAll with page
    Page<User> findAll(Pageable pageable);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    Page<User> findByUsernameContainingIgnoreCase(String searchTerm, Pageable pageable);
    boolean existsByEmail(String email);
    List<User> findByVerificationStatus(User.UserVerification status);

}
