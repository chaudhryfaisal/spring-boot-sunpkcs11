package com.example.pkcs11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

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
}