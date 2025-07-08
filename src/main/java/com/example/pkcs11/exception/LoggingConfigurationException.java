package com.example.pkcs11.exception;

/**
 * Exception thrown when logging configuration operations fail
 */
public class LoggingConfigurationException extends RuntimeException {
    
    public LoggingConfigurationException(String message) {
        super(message);
    }
    
    public LoggingConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}