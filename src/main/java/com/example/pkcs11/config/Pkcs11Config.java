package com.example.pkcs11.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;

@Slf4j
@Configuration
@Profile("!test")
public class Pkcs11Config {

    @Autowired
    private Pkcs11Properties pkcs11Properties;

    @Bean
    public Provider pkcs11Provider() throws Exception {
        log.info("Initializing PKCS#11 provider with library: {}", pkcs11Properties.getLibrary());

        // Create PKCS#11 configuration content
        String configContent = createPkcs11Config();
        log.debug("PKCS#11 configuration: {}", configContent);

        // Create SunPKCS11 provider
        Provider provider = createSunPkcs11Provider(configContent);

        // Register provider with Security
        Security.addProvider(provider);
        log.info("PKCS#11 provider '{}' registered successfully", provider.getName());

        return provider;
    }


    private String createPkcs11Config() {
        StringBuilder config = new StringBuilder();
        config.append("name = ").append(pkcs11Properties.getProviderName()).append("\n");
        config.append("library = ").append(pkcs11Properties.getLibrary()).append("\n");
        if (pkcs11Properties.getSlot() >= 0)
            config.append("slot = ").append(pkcs11Properties.getSlot()).append("\n");
        config.append("attributes = compatibility").append("\n");
        config.append("showInfo = false").append("\n");
        return config.toString();
    }

    private Provider createSunPkcs11Provider(String configContent) throws Exception {
        Path tempConfigFile = null;
        try {
            // Create a temporary file with the PKCS11 configuration
            tempConfigFile = Files.createTempFile("pkcs11-config-", ".cfg");
            Files.write(tempConfigFile, configContent.getBytes());
            log.debug("Created temporary PKCS#11 config file: {}", tempConfigFile);

            // Constants for SunPKCS11 provider
            final String SUN_PKCS11_PROVIDER_NAME = "SunPKCS11";
            final String SUN_PKCS11_CLASSNAME = "sun.security.pkcs11.SunPKCS11";

            // For Java 9+: use new instance + configure method
            Provider prototype = Security.getProvider(SUN_PKCS11_PROVIDER_NAME);
            Class<?> sunPkcs11ProviderClass = Class.forName(SUN_PKCS11_CLASSNAME);
            Method configureMethod = sunPkcs11ProviderClass.getMethod("configure", String.class);

            // Pass the file path (not the content) to the configure method
            return (Provider) configureMethod.invoke(prototype, tempConfigFile.toString());
        } catch (Exception e) {
            log.error("Failed to create SunPKCS11 provider", e);
            throw new RuntimeException("Failed to initialize PKCS#11 provider", e);
        } finally {
            // Clean up the temporary file
            if (tempConfigFile != null) {
                try {
                    Files.deleteIfExists(tempConfigFile);
                    log.debug("Cleaned up temporary PKCS#11 config file: {}", tempConfigFile);
                } catch (Exception e) {
                    log.warn("Failed to delete temporary config file: {}", tempConfigFile, e);
                }
            }
        }
    }
}