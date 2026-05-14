package com.example.bcapi.beer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JpaBackedBeerRepository extends JpaRepository<BeerEntity, UUID>, JpaSpecificationExecutor<BeerEntity> {

    boolean existsByIdAndManufacturerOwner(UUID id, String owner);
}
