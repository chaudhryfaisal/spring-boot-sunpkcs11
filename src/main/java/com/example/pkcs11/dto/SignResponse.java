package com.example.pkcs11.dto;

public class SignResponse {

    private String signature;

    // Default constructor
    public SignResponse() {}

    // Constructor with parameter
    public SignResponse(String signature) {
        this.signature = signature;
    }

    // Getter and setter
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "SignResponse{" +
                "signature='[REDACTED]'" +
                '}';
    }
}