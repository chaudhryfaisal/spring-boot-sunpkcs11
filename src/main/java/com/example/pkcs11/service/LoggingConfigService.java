package com.example.pkcs11.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

/**
 * Service for managing logging configuration dynamically
 */
@Service
public class LoggingConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfigService.class);
    private static final String DEFAULT_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    private static final String LOGS_DIRECTORY = "__logs";
    
    private String currentLogFileName = "application.log";
    private String currentLogLevel = "INFO";
    
    /**
     * Changes the log file name and redirects logging to the new file
     */
    public boolean changeLogFileName(String newLogFileName, String logLevel) {
        try {
            String previousFileName = this.currentLogFileName;
            
            // Ensure logs directory exists
            File logsDir = new File(LOGS_DIRECTORY);
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }
            
            // Get the logback context
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            // Stop existing file appender
            stopExistingFileAppender(loggerContext);
            
            // Create new file appender with the new file name
            createNewFileAppender(loggerContext, newLogFileName, logLevel);
            
            // Update current state
            this.currentLogFileName = newLogFileName;
            this.currentLogLevel = logLevel;
            
            logger.info("Successfully changed log file from '{}' to '{}' with level '{}'", 
                       previousFileName, newLogFileName, logLevel);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to change log file to '{}': {}", newLogFileName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets the current logging configuration
     */
    public String getCurrentLogFileName() {
        return currentLogFileName;
    }
    
    public String getCurrentLogLevel() {
        return currentLogLevel;
    }
    
    /**
     * Stops the existing file appender
     */
    private void stopExistingFileAppender(LoggerContext loggerContext) {
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        
        // Find and stop existing file appenders
        rootLogger.detachAndStopAllAppenders();
    }
    
    /**
     * Creates a new rolling file appender with the specified file name
     */
    private void createNewFileAppender(LoggerContext loggerContext, String fileName, String logLevel) {
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        
        // Set log level
        rootLogger.setLevel(ch.qos.logback.classic.Level.valueOf(logLevel.toUpperCase()));
        
        // Create rolling file appender
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
            new RollingFileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        
        // Set the file path
        String filePath = Paths.get(LOGS_DIRECTORY, fileName).toString();
        fileAppender.setFile(filePath);
        
        // Create rolling policy
        TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = 
            new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(Paths.get(LOGS_DIRECTORY, fileName.replace(".log", "-%d{yyyy-MM-dd}.log")).toString());
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.start();
        
        fileAppender.setRollingPolicy(rollingPolicy);
        
        // Create encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(DEFAULT_LOG_PATTERN);
        encoder.start();
        
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        
        // Add appender to root logger
        rootLogger.addAppender(fileAppender);
        
        // Also add console appender for development
        addConsoleAppender(loggerContext, rootLogger);
    }
    
    /**
     * Adds console appender for development purposes
     */
    private void addConsoleAppender(LoggerContext loggerContext, ch.qos.logback.classic.Logger rootLogger) {
        ch.qos.logback.core.ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
            new ch.qos.logback.core.ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(DEFAULT_LOG_PATTERN);
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        rootLogger.addAppender(consoleAppender);
    }
}