package com.example.pkcs11.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Request DTO for changing logging configuration
 */
public class LoggingConfigRequest {
    
    @NotBlank(message = "Log file name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+\\.log$", message = "Log file name must end with .log and contain only alphanumeric characters, dots, underscores, and hyphens")
    private String logFileName;
    
    private String logLevel = "INFO";
    
    public LoggingConfigRequest() {}
    
    public LoggingConfigRequest(String logFileName, String logLevel) {
        this.logFileName = logFileName;
        this.logLevel = logLevel;
    }
    
    public String getLogFileName() {
        return logFileName;
    }
    
    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}