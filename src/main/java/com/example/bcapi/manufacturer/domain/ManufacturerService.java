package com.example.bcapi.manufacturer.domain;

import com.example.bcapi.common.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
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

    public Manufacturer update(UUID id, ManufacturerDraft draft) {
        countryCodeValidator.validate(draft.originCountry());
        Manufacturer existingManufacturer = findById(id);
        Manufacturer manufacturerToSave = new Manufacturer(
                existingManufacturer.id(),
                draft.name(),
                draft.originCountry(),
                existingManufacturer.createdAt(),
                existingManufacturer.updatedAt()
        );

        return manufacturerRepository.update(manufacturerToSave);
    }

    public void delete(UUID id) {
        manufacturerRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public Manufacturer findById(UUID id) {
        return manufacturerRepository.findById(id).orElseThrow(() -> new ManufacturerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Manufacturer> findAll(ManufacturerQuery manufacturerQuery) {
        return manufacturerRepository.findAll(manufacturerQuery);
    }
}
