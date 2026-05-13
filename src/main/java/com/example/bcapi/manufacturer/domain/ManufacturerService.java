package com.example.bcapi.manufacturer.domain;

import org.springframework.stereotype.Service;

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
}
