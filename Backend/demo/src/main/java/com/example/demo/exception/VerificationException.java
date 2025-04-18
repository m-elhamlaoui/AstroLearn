package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN) // Or BAD_REQUEST depending on context
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}