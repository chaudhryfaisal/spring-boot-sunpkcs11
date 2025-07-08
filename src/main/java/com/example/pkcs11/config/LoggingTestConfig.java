package com.example.pkcs11.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for logging test profile that excludes PKCS#11 dependencies
 */
@Configuration
@Profile("logging-test")
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "logging-test")
public class LoggingTestConfig {
    // This configuration is used when running with logging-test profile
    // It excludes PKCS#11 related beans to allow testing logging functionality
}