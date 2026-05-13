package com.example.bcapi.beer.domain;

import java.util.UUID;

public class BeerNotFoundException extends RuntimeException {
    public BeerNotFoundException(UUID id) {
        super("Beer not found: " + id);
    }
}
