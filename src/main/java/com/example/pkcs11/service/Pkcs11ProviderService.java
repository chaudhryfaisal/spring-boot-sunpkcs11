package com.example.pkcs11.service;

import com.example.pkcs11.config.Pkcs11Properties;
import com.example.pkcs11.exception.KeyNotFoundException;
import com.example.pkcs11.exception.SigningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Profile("!test")
public class Pkcs11ProviderService {

    @Autowired
    private Provider pkcs11Provider;

    @Autowired
    private Pkcs11Properties pkcs11Properties;

    private Map<String, PrivateKey> keyMap = new HashMap<>();

    // Cache for key store to avoid repeated PIN authentication
    private volatile KeyStore cachedKeyStore;

    /**
     * Retrieves a private key from the PKCS#11 token
     */
    public PrivateKey getPrivateKey(String keyLabel) {
        try {
            PrivateKey privateKey = keyMap.get(keyLabel);
            if (privateKey == null) {
                KeyStore keyStore = getKeyStore();

                // Find the key by alias
                privateKey = findPrivateKeyByLabel(keyStore, keyLabel);
                if (privateKey == null) {
                    throw new KeyNotFoundException("Private key not found for label: " + keyLabel);
                }

                log.debug("Successfully retrieved private key for label: {}", keyLabel);

                keyMap.put(keyLabel, privateKey);
            }
            return privateKey;

        } catch (KeyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve private key for label: {}", keyLabel, e);
            throw new SigningException("Failed to retrieve private key: " + e.getMessage(), e);
        }
    }

    /**
     * Gets or creates a KeyStore for the PKCS#11 provider
     */
    private KeyStore getKeyStore() throws Exception {
        if (cachedKeyStore == null) {
            synchronized (this) {
                if (cachedKeyStore == null) {
                    try {
                        KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
                        char[] pin = pkcs11Properties.getPin().toCharArray();
                        keyStore.load(null, pin);
                        log.debug("KeyStore loaded successfully with provider PIN");
                        debugKeystore(keyStore);
                        cachedKeyStore = keyStore;
                    } catch (Exception e) {
                        log.error("Failed to load KeyStore with provider PIN", e);
                        throw new SigningException("Failed to load KeyStore: " + e.getMessage(), e);
                    }
                }
            }
        }
        return cachedKeyStore;
    }

    /**
     * Finds a private key by label in the KeyStore
     */
    private PrivateKey findPrivateKeyByLabel(KeyStore keyStore, String keyLabel) throws Exception {
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            log.debug("Checking alias: {}", alias);

            // Check if this alias matches our key label or contains it
            if (alias.equals(keyLabel) || alias.contains(keyLabel)) {
                if (keyStore.isKeyEntry(alias)) {
                    Key key = keyStore.getKey(alias, null); // PKCS#11 doesn't use key passwords
                    if (key instanceof PrivateKey) {
                        log.debug("Found private key with alias: {}", alias);
                        return (PrivateKey) key;
                    }
                }
            }
        }

        // If exact match not found, try to find by certificate subject or other attributes
        return findPrivateKeyByAttributes(keyStore, keyLabel);
    }

    /**
     * Alternative method to find private key by certificate attributes
     */
    private PrivateKey findPrivateKeyByAttributes(KeyStore keyStore, String keyLabel) throws Exception {
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            if (keyStore.isKeyEntry(alias)) {
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null) {
                    // You could add more sophisticated matching logic here
                    // For now, we'll just check if the alias contains the label
                    if (alias.toLowerCase().contains(keyLabel.toLowerCase())) {
                        Key key = keyStore.getKey(alias, null);
                        if (key instanceof PrivateKey) {
                            log.debug("Found private key with matching alias: {}", alias);
                            return (PrivateKey) key;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the algorithm name for signing based on key type and private key
     */
    public String getSigningAlgorithm(String algorithmType, PrivateKey privateKey) {
        switch (algorithmType.toUpperCase()) {
            case "RSA":
                return "SHA256withRSA";
            case "EC":
                return "SHA256withECDSA";
            default:
                throw new IllegalArgumentException("Unsupported algorithm type: " + algorithmType);
        }
    }

    /**
     * Validates that the key type matches the private key algorithm
     */
    public void validateKeyType(String expectedType, PrivateKey privateKey) {
        String keyAlgorithm = privateKey.getAlgorithm();

        boolean isValid = false;
        switch (expectedType.toUpperCase()) {
            case "RSA":
                isValid = "RSA".equals(keyAlgorithm);
                break;
            case "EC":
                isValid = "EC".equals(keyAlgorithm) || "ECDSA".equals(keyAlgorithm);
                break;
        }

        if (!isValid) {
            throw new IllegalArgumentException(
                    String.format("Key type mismatch. Expected: %s, Found: %s", expectedType, keyAlgorithm)
            );
        }
    }

    /**
     * Clears the key store cache (useful for testing or configuration changes)
     */
    public void clearCache() {
        synchronized (this) {
            cachedKeyStore = null;
        }
        log.info("KeyStore cache cleared");
    }

    static void debugKeystore(KeyStore keyStore) throws KeyStoreException {
        log.info("keystore:debug");
        int count = 0;
        Enumeration<String> enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            count++;
            log.info("\tkeystore:debug:alias:" + enumeration.nextElement());
        }
        if (count == 0) {
            log.warn("\tkeystore:debug:alias count=" + count);
        } else log.info("\tkeystore:debug:alias count=" + count);
    }

}