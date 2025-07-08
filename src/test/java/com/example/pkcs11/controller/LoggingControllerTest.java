package com.example.pkcs11.controller;

import com.example.pkcs11.dto.LoggingConfigRequest;
import com.example.pkcs11.service.LoggingConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoggingController.class)
class LoggingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoggingConfigService loggingConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testChangeLogFile_Success() throws Exception {
        // Given
        LoggingConfigRequest request = new LoggingConfigRequest("new-app.log", "DEBUG");
        when(loggingConfigService.getCurrentLogFileName()).thenReturn("application.log");
        when(loggingConfigService.changeLogFileName(anyString(), anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/logging/change-file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLogFileName").value("new-app.log"))
                .andExpect(jsonPath("$.currentLogLevel").value("DEBUG"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testChangeLogFile_Failure() throws Exception {
        // Given
        LoggingConfigRequest request = new LoggingConfigRequest("invalid-file.log", "INFO");
        when(loggingConfigService.getCurrentLogFileName()).thenReturn("application.log");
        when(loggingConfigService.getCurrentLogLevel()).thenReturn("INFO");
        when(loggingConfigService.changeLogFileName(anyString(), anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/v1/logging/change-file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void testChangeLogFile_InvalidRequest() throws Exception {
        // Given
        LoggingConfigRequest request = new LoggingConfigRequest("", "INFO");

        // When & Then
        mockMvc.perform(post("/v1/logging/change-file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCurrentLoggingConfig() throws Exception {
        // Given
        when(loggingConfigService.getCurrentLogFileName()).thenReturn("application.log");
        when(loggingConfigService.getCurrentLogLevel()).thenReturn("INFO");

        // When & Then
        mockMvc.perform(get("/v1/logging/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLogFileName").value("application.log"))
                .andExpect(jsonPath("$.currentLogLevel").value("INFO"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testGenerateTestLogs() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/logging/test")
                .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("Generated 3 test log messages at various levels"));
    }
}