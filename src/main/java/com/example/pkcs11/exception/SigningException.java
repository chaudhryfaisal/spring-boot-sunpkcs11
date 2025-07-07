package com.example.pkcs11.exception;

public class SigningException extends RuntimeException {
    
    public SigningException(String message) {
        super(message);
    }
    
    public SigningException(String message, Throwable cause) {
        super(message, cause);
    }
}