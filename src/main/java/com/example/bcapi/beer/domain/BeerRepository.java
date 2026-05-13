package com.example.bcapi.beer.domain;

import com.example.bcapi.common.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerRepository {

    Beer create(BeerDraft draft);

    Beer update(Beer beer);

    Optional<Beer> findById(UUID id);

    Page<Beer> findAll(BeerQuery query);

    void delete(UUID id);
}
