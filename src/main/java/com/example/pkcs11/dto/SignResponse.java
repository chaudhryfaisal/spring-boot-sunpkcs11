package com.example.pkcs11.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignResponse {

    private String signature;

    @Override
    public String toString() {
        return "SignResponse{" +
                "signature='[REDACTED]'" +
                '}';
    }
}