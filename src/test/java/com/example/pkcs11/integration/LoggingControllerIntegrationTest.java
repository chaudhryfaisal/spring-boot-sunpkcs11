package com.example.pkcs11.integration;

import com.example.pkcs11.config.TestPkcs11Config;
import com.example.pkcs11.dto.LoggingConfigRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestPkcs11Config.class)
class LoggingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testChangeLogFileIntegration() throws Exception {
        // Given
        String testLogFileName = "integration-test.log";
        LoggingConfigRequest request = new LoggingConfigRequest(testLogFileName, "DEBUG");

        // When - Change log file
        mockMvc.perform(post("/v1/logging/change-file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLogFileName").value(testLogFileName))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Then - Generate test logs to verify file creation
        mockMvc.perform(post("/v1/logging/test")
                .param("count", "3"))
                .andExpect(status().isOk());

        // Verify log file was created
        Path logFilePath = Paths.get("logs", testLogFileName);
        assertTrue(Files.exists(logFilePath), "Log file should be created");

        // Verify current configuration
        mockMvc.perform(get("/v1/logging/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLogFileName").value(testLogFileName))
                .andExpect(jsonPath("$.currentLogLevel").value("DEBUG"));

        // Cleanup
        try {
            Files.deleteIfExists(logFilePath);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testInvalidLogFileName() throws Exception {
        // Given
        LoggingConfigRequest request = new LoggingConfigRequest("invalid-file.txt", "INFO");

        // When & Then
        mockMvc.perform(post("/v1/logging/change-file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}