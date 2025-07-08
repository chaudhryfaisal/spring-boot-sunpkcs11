package com.example.pkcs11.controller;

import com.example.pkcs11.dto.LoggingConfigRequest;
import com.example.pkcs11.dto.LoggingConfigResponse;
import com.example.pkcs11.service.LoggingConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for managing logging configuration
 */
@RestController
@RequestMapping("/v1/logging")
public class LoggingController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);
    
    private final LoggingConfigService loggingConfigService;
    
    @Autowired
    public LoggingController(LoggingConfigService loggingConfigService) {
        this.loggingConfigService = loggingConfigService;
    }
    
    /**
     * Changes the log file name and optionally the log level
     */
    @PostMapping("/change-file")
    public ResponseEntity<LoggingConfigResponse> changeLogFile(@Valid @RequestBody LoggingConfigRequest request) {
        logger.info("Received request to change log file to: {}", request.getLogFileName());
        
        String previousFileName = loggingConfigService.getCurrentLogFileName();
        
        boolean success = loggingConfigService.changeLogFileName(
            request.getLogFileName(), 
            request.getLogLevel()
        );
        
        if (success) {
            LoggingConfigResponse response = new LoggingConfigResponse(
                request.getLogFileName(),
                request.getLogLevel(),
                previousFileName,
                "SUCCESS",
                "Log file changed successfully"
            );
            
            logger.info("Log file changed successfully from '{}' to '{}'", 
                       previousFileName, request.getLogFileName());
            
            return ResponseEntity.ok(response);
        } else {
            LoggingConfigResponse response = new LoggingConfigResponse(
                loggingConfigService.getCurrentLogFileName(),
                loggingConfigService.getCurrentLogLevel(),
                previousFileName,
                "ERROR",
                "Failed to change log file"
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Gets the current logging configuration
     */
    @GetMapping("/current")
    public ResponseEntity<LoggingConfigResponse> getCurrentLoggingConfig() {
        LoggingConfigResponse response = new LoggingConfigResponse(
            loggingConfigService.getCurrentLogFileName(),
            loggingConfigService.getCurrentLogLevel(),
            null,
            "SUCCESS",
            "Current logging configuration retrieved"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to generate log messages for testing
     */
    @PostMapping("/test")
    public ResponseEntity<String> generateTestLogs(@RequestParam(defaultValue = "5") int count) {
        logger.info("Generating {} test log messages", count);
        
        for (int i = 1; i <= count; i++) {
            logger.trace("TRACE level test message #{}", i);
            logger.debug("DEBUG level test message #{}", i);
            logger.info("INFO level test message #{}", i);
            logger.warn("WARN level test message #{}", i);
            if (i % 3 == 0) {
                logger.error("ERROR level test message #{}", i);
            }
        }
        
        return ResponseEntity.ok("Generated " + count + " test log messages at various levels");
    }
}