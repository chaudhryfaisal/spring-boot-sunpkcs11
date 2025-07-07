package com.example.pkcs11.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class SignRequest {

    @NotBlank(message = "Key label is required")
    private String keyLabel;

    @NotBlank(message = "Algorithm is required")
    @Pattern(regexp = "RSA|EC", message = "Algorithm must be either 'RSA' or 'EC'")
    private String algorithm;

    @NotBlank(message = "Data is required")
    private String data;

    // Default constructor
    public SignRequest() {}

    // Constructor with parameters
    public SignRequest(String keyLabel, String algorithm, String data) {
        this.keyLabel = keyLabel;
        this.algorithm = algorithm;
        this.data = data;
    }

    // Getters and setters
    public String getKeyLabel() {
        return keyLabel;
    }

    public void setKeyLabel(String keyLabel) {
        this.keyLabel = keyLabel;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SignRequest{" +
                "keyLabel='" + keyLabel + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", data='[REDACTED]'" +
                '}';
    }
}