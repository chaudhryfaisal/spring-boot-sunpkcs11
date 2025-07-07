package com.example.pkcs11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "pkcs11")
public class Pkcs11Properties {

    @NotBlank
    private String library;

    private int slot = 0;

    @NotBlank
    private String providerName = "PKCS11-Provider";

    @NotBlank
    private String pin;

    @Valid
    @NotEmpty
    private List<KeyConfig> keys;

    @Data
    public static class KeyConfig {
        @NotBlank
        private String label;

        @NotNull
        private KeyType type;
    }

    public enum KeyType {
        RSA, EC
    }
}