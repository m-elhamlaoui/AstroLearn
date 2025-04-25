package com.example.demo.payloadRequest;


import lombok.Data;

@Data

public class LoginRequest {

    private String email;
    private String password;

}