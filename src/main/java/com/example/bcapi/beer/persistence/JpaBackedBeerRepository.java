package com.example.bcapi.beer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaBackedBeerRepository extends JpaRepository<BeerEntity, UUID> {
}
