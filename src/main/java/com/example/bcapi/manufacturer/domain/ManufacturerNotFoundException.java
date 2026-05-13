package com.example.bcapi.manufacturer.domain;

import java.util.UUID;

public class ManufacturerNotFoundException extends RuntimeException {
    public ManufacturerNotFoundException(UUID id) {
        super("Manufacturer not found: " + id);
    }
}
