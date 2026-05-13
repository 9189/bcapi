package com.example.bcapi.manufacturer.domain;

import com.example.bcapi.common.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ManufacturerRepository {
    Manufacturer create(ManufacturerDraft manufacturer);
    Manufacturer update(Manufacturer manufacturer);
    void delete(UUID id);
    Optional<Manufacturer> findById(UUID id);
    Page<Manufacturer> findAll(ManufacturerQuery manufacturerQuery);
}
