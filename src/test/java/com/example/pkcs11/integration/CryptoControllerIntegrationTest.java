package com.example.pkcs11.integration;

import com.example.pkcs11.config.TestPkcs11Config;
import com.example.pkcs11.dto.SignRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class CryptoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/v1/crypto/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("PKCS#11 Signing Service"));
    }

    @Test
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/v1/crypto/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("PKCS#11 Signing Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.supportedAlgorithms").isArray());
    }

    @Test
    void testSignEndpoint_InvalidRequest_MissingKeyLabel() throws Exception {
        SignRequest request = new SignRequest();
        request.setAlgorithm("RSA");
        request.setData(Base64.getEncoder().encodeToString("test data".getBytes()));

        mockMvc.perform(post("/v1/crypto/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignEndpoint_InvalidRequest_InvalidAlgorithm() throws Exception {
        SignRequest request = new SignRequest();
        request.setKeyLabel("test-key");
        request.setAlgorithm("INVALID");
        request.setData(Base64.getEncoder().encodeToString("test data".getBytes()));

        mockMvc.perform(post("/v1/crypto/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignEndpoint_InvalidRequest_MissingData() throws Exception {
        SignRequest request = new SignRequest();
        request.setKeyLabel("test-key");
        request.setAlgorithm("RSA");

        mockMvc.perform(post("/v1/crypto/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignEndpoint_InvalidRequest_EmptyBody() throws Exception {
        mockMvc.perform(post("/v1/crypto/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignEndpoint_InvalidRequest_MalformedJson() throws Exception {
        mockMvc.perform(post("/v1/crypto/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    // Note: Actual signing tests would require a real PKCS#11 setup
    // These tests focus on request validation and endpoint availability
}