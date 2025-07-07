package com.example.pkcs11.config;

import com.example.pkcs11.service.Pkcs11ProviderService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@Profile("test")
public class TestPkcs11Config {

    @Bean
    @Primary
    public Provider pkcs11Provider() {
        // Create a mock provider for tests
        Provider mockProvider = Mockito.mock(Provider.class);
        Mockito.when(mockProvider.getName()).thenReturn("MockPKCS11");
        Mockito.when(mockProvider.getInfo()).thenReturn("Mock PKCS#11 Provider for Testing");
        return mockProvider;
    }

    @Bean
    @Primary
    public Pkcs11ProviderService pkcs11ProviderService() {
        return Mockito.mock(Pkcs11ProviderService.class);
    }

    @Bean
    @Primary
    public Map<String, Pkcs11Properties.KeyConfig> keyConfigMap() {
        Map<String, Pkcs11Properties.KeyConfig> keyMap = new HashMap<>();
        
        // Create mock key configurations for testing
        Pkcs11Properties.KeyConfig rsaKey = new Pkcs11Properties.KeyConfig();
        rsaKey.setLabel("rsa-2048");
        rsaKey.setType(Pkcs11Properties.KeyType.RSA);
        
        Pkcs11Properties.KeyConfig ecKey = new Pkcs11Properties.KeyConfig();
        ecKey.setLabel("ecc-256");
        ecKey.setType(Pkcs11Properties.KeyType.EC);
        
        keyMap.put("rsa-2048", rsaKey);
        keyMap.put("ecc-256", ecKey);
        
        return keyMap;
    }
}