package com.example.pkcs11.config;

import com.example.pkcs11.service.Pkcs11ProviderService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.security.Provider;

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

}