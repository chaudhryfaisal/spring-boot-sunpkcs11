package com.example.pkcs11.controller;

import com.example.pkcs11.dto.SignRequest;
import com.example.pkcs11.dto.SignResponse;
import com.example.pkcs11.service.SigningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/crypto")
public class CryptoController {

    private static final Logger logger = LoggerFactory.getLogger(CryptoController.class);

    @Autowired
    private SigningService signingService;

    /**
     * Signs data using PKCS#11 token
     */
    @PostMapping("/sign")
    public ResponseEntity<SignResponse> signData(@Valid @RequestBody SignRequest request) {
        logger.info("Received signing request for key: {}, algorithm: {}", 
                   request.getKeyLabel(), request.getAlgorithm());
        
        try {
            // Validate input data format
            signingService.validateInputData(request.getData());
            
            // Perform signing operation
            String signature = signingService.signData(
                request.getKeyLabel(),
                request.getAlgorithm(),
                request.getData()
            );
            
            SignResponse response = new SignResponse(signature);
            
            logger.info("Successfully completed signing request for key: {}", request.getKeyLabel());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to process signing request for key: {}", request.getKeyLabel(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "PKCS#11 Signing Service");
        status.put("info", signingService.getSigningInfo());
        return ResponseEntity.ok(status);
    }

    /**
     * Get service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "PKCS#11 Signing Service");
        info.put("version", "1.0.0");
        info.put("description", "REST API for signing data using PKCS#11 tokens");
        info.put("supportedAlgorithms", new String[]{"RSA", "EC"});
        info.put("endpoints", Map.of(
            "sign", "POST /v1/crypto/sign",
            "health", "GET /v1/crypto/health",
            "info", "GET /v1/crypto/info"
        ));
        return ResponseEntity.ok(info);
    }
}