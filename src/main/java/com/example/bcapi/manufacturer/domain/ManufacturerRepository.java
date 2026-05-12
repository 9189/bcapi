package com.example.bcapi.manufacturer.domain;

public interface ManufacturerRepository {
    Manufacturer create(ManufacturerDraft manufacturer);
}
