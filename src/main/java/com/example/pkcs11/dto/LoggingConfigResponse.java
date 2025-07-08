package com.example.pkcs11.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for logging configuration operations
 */
public class LoggingConfigResponse {
    
    private String currentLogFileName;
    private String currentLogLevel;
    private String previousLogFileName;
    private LocalDateTime changedAt;
    private String status;
    private String message;
    
    public LoggingConfigResponse() {}
    
    public LoggingConfigResponse(String currentLogFileName, String currentLogLevel, 
                               String previousLogFileName, String status, String message) {
        this.currentLogFileName = currentLogFileName;
        this.currentLogLevel = currentLogLevel;
        this.previousLogFileName = previousLogFileName;
        this.changedAt = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }
    
    public String getCurrentLogFileName() {
        return currentLogFileName;
    }
    
    public void setCurrentLogFileName(String currentLogFileName) {
        this.currentLogFileName = currentLogFileName;
    }
    
    public String getCurrentLogLevel() {
        return currentLogLevel;
    }
    
    public void setCurrentLogLevel(String currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }
    
    public String getPreviousLogFileName() {
        return previousLogFileName;
    }
    
    public void setPreviousLogFileName(String previousLogFileName) {
        this.previousLogFileName = previousLogFileName;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}