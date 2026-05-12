package com.example.bcapi.manufacturer.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Manufacturer(
        UUID id,
        String name,
        String originCountry,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
