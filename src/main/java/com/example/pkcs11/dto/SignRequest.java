package com.example.pkcs11.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignRequest {

    @NotBlank(message = "Key label is required")
    private String keyLabel;

    @NotBlank(message = "Algorithm is required")
    @Pattern(regexp = "RSA|EC", message = "Algorithm must be either 'RSA' or 'EC'")
    private String algorithm;

    @NotBlank(message = "Data is required")
    private String data;

    @Override
    public String toString() {
        return "SignRequest{" +
                "keyLabel='" + keyLabel + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", data='[REDACTED]'" +
                '}';
    }
}