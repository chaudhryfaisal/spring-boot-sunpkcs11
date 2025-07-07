package com.example.pkcs11.service;

import com.example.pkcs11.exception.SigningException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SigningServiceTest {

    @Mock
    private Pkcs11ProviderService pkcs11ProviderService;

    @Mock
    private PrivateKey mockPrivateKey;

    @InjectMocks
    private SigningService signingService;

    private String validBase64Data;

    @BeforeEach
    void setUp() {
        validBase64Data = Base64.getEncoder().encodeToString("Hello, World!".getBytes());
    }

    @Test
    void testValidateInputData_ValidBase64() {
        assertDoesNotThrow(() -> signingService.validateInputData(validBase64Data));
    }

    @Test
    void testValidateInputData_NullData() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> signingService.validateInputData(null)
        );
        assertEquals("Input data cannot be null or empty", exception.getMessage());
    }

    @Test
    void testValidateInputData_EmptyData() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> signingService.validateInputData("")
        );
        assertEquals("Input data cannot be null or empty", exception.getMessage());
    }

    @Test
    void testValidateInputData_InvalidBase64() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> signingService.validateInputData("invalid-base64!")
        );
        assertTrue(exception.getMessage().contains("Input data must be valid base64 encoded"));
    }

    @Test
    void testGetSigningInfo() {
        String info = signingService.getSigningInfo();
        assertNotNull(info);
        assertTrue(info.contains("PKCS#11"));
        assertTrue(info.contains("RSA"));
        assertTrue(info.contains("EC"));
    }

    @Test
    void testSignData_KeyNotFound() {
        when(pkcs11ProviderService.getPrivateKey("nonexistent-key"))
            .thenThrow(new RuntimeException("Key not found"));

        assertThrows(SigningException.class, () -> 
            signingService.signData("nonexistent-key", "RSA", validBase64Data)
        );
    }

    @Test
    void testSignData_InvalidAlgorithm() {
        when(pkcs11ProviderService.getPrivateKey("test-key")).thenReturn(mockPrivateKey);
        doThrow(new IllegalArgumentException("Unsupported algorithm"))
            .when(pkcs11ProviderService).validateKeyType(anyString(), any());

        assertThrows(SigningException.class, () -> 
            signingService.signData("test-key", "INVALID", validBase64Data)
        );
    }
}