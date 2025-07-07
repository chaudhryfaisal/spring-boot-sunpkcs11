package com.example.pkcs11.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "pkcs11")
public class Pkcs11Properties {

    @NotBlank
    private String library;

    private int slot = 0;

    @NotBlank
    private String providerName = "PKCS11-Provider";

    @Valid
    @NotEmpty
    private List<KeyConfig> keys;

    // Getters and setters
    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public List<KeyConfig> getKeys() {
        return keys;
    }

    public void setKeys(List<KeyConfig> keys) {
        this.keys = keys;
    }

    public static class KeyConfig {
        @NotBlank
        private String label;

        @NotBlank
        private String pin;

        @NotNull
        private KeyType type;

        // Getters and setters
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPin() {
            return pin;
        }

        public void setPin(String pin) {
            this.pin = pin;
        }

        public KeyType getType() {
            return type;
        }

        public void setType(KeyType type) {
            this.type = type;
        }
    }

    public enum KeyType {
        RSA, EC
    }
}