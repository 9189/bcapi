package com.example.bcapi.beer.domain;

public record BeerQuery(
        int page,
        int size,
        String sortBy,
        SortDirection sortDirection
) {
}
