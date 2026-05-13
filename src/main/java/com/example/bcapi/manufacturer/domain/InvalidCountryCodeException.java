package com.example.bcapi.manufacturer.domain;

public class InvalidCountryCodeException extends RuntimeException {
    public InvalidCountryCodeException(String message) {
        super(message);
    }
}
