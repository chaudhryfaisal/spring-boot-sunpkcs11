package com.example.pkcs11.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Pkcs11Config {

    private static final Logger logger = LoggerFactory.getLogger(Pkcs11Config.class);

    @Autowired
    private Pkcs11Properties pkcs11Properties;

    @Bean
    public Provider pkcs11Provider() throws Exception {
        logger.info("Initializing PKCS#11 provider with library: {}", pkcs11Properties.getLibrary());
        
        // Create PKCS#11 configuration content
        String configContent = createPkcs11Config();
        logger.debug("PKCS#11 configuration: {}", configContent);
        
        // Create SunPKCS11 provider
        Provider provider = createSunPkcs11Provider(configContent);
        
        // Register provider with Security
        Security.addProvider(provider);
        logger.info("PKCS#11 provider '{}' registered successfully", provider.getName());
        
        return provider;
    }

    @Bean
    public Map<String, Pkcs11Properties.KeyConfig> keyConfigMap() {
        Map<String, Pkcs11Properties.KeyConfig> keyMap = new HashMap<>();
        for (Pkcs11Properties.KeyConfig keyConfig : pkcs11Properties.getKeys()) {
            keyMap.put(keyConfig.getLabel(), keyConfig);
        }
        logger.info("Loaded {} key configurations", keyMap.size());
        return keyMap;
    }

    private String createPkcs11Config() {
        StringBuilder config = new StringBuilder();
        config.append("name = ").append(pkcs11Properties.getProviderName()).append("\n");
        config.append("library = ").append(pkcs11Properties.getLibrary()).append("\n");
        config.append("slot = ").append(pkcs11Properties.getSlot()).append("\n");
        config.append("attributes = compatibility").append("\n");
        config.append("showInfo = false").append("\n");
        return config.toString();
    }

    private Provider createSunPkcs11Provider(String configContent) throws Exception {
        try {
            // Use reflection to create SunPKCS11 provider to avoid direct dependency
            Class<?> sunPkcs11Class = Class.forName("sun.security.pkcs11.SunPKCS11");
            
            // Create provider with configuration stream
            ByteArrayInputStream configStream = new ByteArrayInputStream(configContent.getBytes());
            return (Provider) sunPkcs11Class.getConstructor(java.io.InputStream.class)
                    .newInstance(configStream);
        } catch (Exception e) {
            logger.error("Failed to create SunPKCS11 provider", e);
            throw new RuntimeException("Failed to initialize PKCS#11 provider", e);
        }
    }
}