package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;



    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/username/{username}")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @PathVariable("username") String usernameQuery, // Parameter from URL (?username=test)
            Pageable pageable // Spring automatically populates this (e.g., ?page=0&size=10&sort=username,asc)
    ) {
        Page<UserDTO> results = userService.searchUsersByUsername(usernameQuery, pageable);
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}/{points}/experience")
    public ResponseEntity<Void> addExperiencePoints(@PathVariable Long id, @PathVariable int points) {
        userService.addExperiencePoints(id, points);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/verification/request")
    public ResponseEntity<Void> requestVerification(@PathVariable Long id) {
        userService.requestVerification(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/verification/approve/{userId}")
    public ResponseEntity<Void> approveVerification(@PathVariable Long userId) {
        userService.approveVerification(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/verification/reject/{userId}")
    public ResponseEntity<Void> rejectVerification(@PathVariable Long userId, @RequestParam(required = false) String reason) {
        userService.rejectVerification(userId, reason);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/verification-status")
    public ResponseEntity<List<UserDTO>> getUsersByVerificationStatus(@RequestParam User.UserVerification status) {
        return ResponseEntity.ok(userService.getUsersByVerificationStatus(status));
    }
}