package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
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

    @PostMapping("/{id}/experience")
    public ResponseEntity<Void> addExperiencePoints(@PathVariable Long id, @RequestParam int points) {
        userService.addExperiencePoints(id, points);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/verification/request")
    public ResponseEntity<Void> requestVerification(@PathVariable Long id) {
        userService.requestVerification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{adminId}/verification/approve/{userId}")
    public ResponseEntity<Void> approveVerification(@PathVariable Long adminId, @PathVariable Long userId) {
        userService.approveVerification(adminId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{adminId}/verification/reject/{userId}")
    public ResponseEntity<Void> rejectVerification(@PathVariable Long adminId, @PathVariable Long userId) {
        userService.rejectVerification(adminId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verification-status")
    public ResponseEntity<List<UserDTO>> getUsersByVerificationStatus(@RequestParam User.UserVerification status) {
        return ResponseEntity.ok(userService.getUsersByVerificationStatus(status));
    }
}