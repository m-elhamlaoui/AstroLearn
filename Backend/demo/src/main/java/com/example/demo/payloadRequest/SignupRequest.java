package com.example.demo.payloadRequest;


import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String role;
}