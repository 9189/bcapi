package com.example.bcapi.beer.domain;

import com.example.bcapi.manufacturer.domain.Manufacturer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Beer(
        UUID id,
        String name,
        String type,
        double abv,
        String description,
        Manufacturer manufacturer,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
