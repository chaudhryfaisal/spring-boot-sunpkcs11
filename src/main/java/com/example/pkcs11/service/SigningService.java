package com.example.pkcs11.service;

import com.example.pkcs11.exception.SigningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class SigningService {

    @Autowired
    private Pkcs11ProviderService pkcs11ProviderService;

    /**
     * Signs the provided data using the specified key
     */
    public String signData(String keyLabel, String algorithmType, String base64Data, @NotNull String name) {
        try {
            log.debug("Starting signing operation for key: {}, algorithm: {}", keyLabel, algorithmType);

            // Decode the input data
            byte[] dataToSign = Base64.getDecoder().decode(base64Data);
            log.debug("Decoded {} bytes of data to sign", dataToSign.length);

            // Get the private key
            PrivateKey privateKey = pkcs11ProviderService.getPrivateKey(keyLabel);

            // Validate key type matches the requested algorithm
            pkcs11ProviderService.validateKeyType(algorithmType, privateKey);

            // Get the appropriate signing algorithm
            String signingAlgorithm = pkcs11ProviderService.getSigningAlgorithm(algorithmType, privateKey);
            log.debug("Using signing algorithm: {}", signingAlgorithm);
            Instant start = Instant.now();
            // Perform the signing operation
            byte[] signatureBytes = performSigning(dataToSign, privateKey, signingAlgorithm);
            Duration elapsed = Duration.between(start, Instant.now());
            // Encode the signature as base64
            String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);

            log.info("Successfully signed data for key: {}, signature length: {} bytes, duration: {} ms {}",
                    keyLabel, signatureBytes.length, elapsed.toMillis(), name);

//            log.info("Successfully signed data for key: {}, signature length: {} bytes, duration: {} µs",
//                       keyLabel, signatureBytes.length,  elapsed.toNanos() / 1_000);

            return base64Signature;

        } catch (SigningException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to sign data for key: {}", keyLabel, e);
            throw new SigningException("Signing operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Performs the actual signing operation
     */
    private byte[] performSigning(byte[] dataToSign, PrivateKey privateKey, String algorithm) throws Exception {
        try {
            // Create signature instance
            Signature signature = Signature.getInstance(algorithm);

            // Initialize with private key
            signature.initSign(privateKey);

            // Update with data to sign
            signature.update(dataToSign);

            // Generate signature
            byte[] signatureBytes = signature.sign();

            log.debug("Generated signature of {} bytes using algorithm: {}",
                    signatureBytes.length, algorithm);

            return signatureBytes;

        } catch (Exception e) {
            log.error("Failed to perform signing operation with algorithm: {}", algorithm, e);
            throw new SigningException("Cryptographic signing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the input data format
     */
    public void validateInputData(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("Input data cannot be null or empty");
        }

        try {
            Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Input data must be valid base64 encoded", e);
        }
    }

    /**
     * Gets information about the signing capabilities
     */
    public String getSigningInfo() {
        return "PKCS#11 Signing Service - Supports RSA and EC algorithms with SHA-256";
    }
}