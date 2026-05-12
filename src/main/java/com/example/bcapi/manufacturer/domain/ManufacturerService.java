package com.example.bcapi.manufacturer.domain;

import org.springframework.stereotype.Service;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    public ManufacturerService(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    public Manufacturer create(ManufacturerDraft manufacturer) {
        return manufacturerRepository.create(manufacturer);
    }
}
