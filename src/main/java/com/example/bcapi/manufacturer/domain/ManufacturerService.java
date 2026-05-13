package com.example.bcapi.manufacturer.domain;

import com.example.bcapi.common.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final CountryCodeValidator countryCodeValidator;

    public ManufacturerService(ManufacturerRepository manufacturerRepository, CountryCodeValidator countryCodeValidator) {
        this.manufacturerRepository = manufacturerRepository;
        this.countryCodeValidator = countryCodeValidator;
    }

    public Manufacturer create(ManufacturerDraft draft) {
        countryCodeValidator.validate(draft.originCountry());

        return manufacturerRepository.create(draft);
    }

    public Manufacturer update(Manufacturer draft) {
        countryCodeValidator.validate(draft.originCountry());

        return manufacturerRepository.update(draft);
    }

    public void delete(UUID id) {
        manufacturerRepository.delete(id);
    }

    public Manufacturer findById(UUID id) {
        return manufacturerRepository.findById(id).orElseThrow(() -> new ManufacturerNotFoundException(id));
    }

    public Page<Manufacturer> findAll(ManufacturerQuery manufacturerQuery) {
        return manufacturerRepository.findAll(manufacturerQuery);
    }
}
