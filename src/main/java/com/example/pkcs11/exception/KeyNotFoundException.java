package com.example.pkcs11.exception;

public class KeyNotFoundException extends RuntimeException {
    
    public KeyNotFoundException(String message) {
        super(message);
    }
    
    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}