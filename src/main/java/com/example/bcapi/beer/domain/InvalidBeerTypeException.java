package com.example.bcapi.beer.domain;

public class InvalidBeerTypeException extends RuntimeException {
    public InvalidBeerTypeException(String type) {
        super("Invalid beer type: " + type);
    }
}
