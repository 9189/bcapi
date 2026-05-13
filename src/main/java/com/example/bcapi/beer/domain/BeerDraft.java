package com.example.bcapi.beer.domain;

import java.util.UUID;

public record BeerDraft(
        String name,
        String type,
        double abv,
        String description,
        UUID manufacturerId
) {
}
