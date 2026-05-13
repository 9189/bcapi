package com.example.bcapi.manufacturer.persistence;

import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.manufacturer.domain.ManufacturerRepository;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerRepositoryAdapter implements ManufacturerRepository {

    private final JpaBackedManufacturerRepository jpaRepository;
    private final ManufacturerEntityMapper mapper;

    ManufacturerRepositoryAdapter(
            JpaBackedManufacturerRepository jpaRepository,
            ManufacturerEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Manufacturer create(ManufacturerDraft draft) {
        ManufacturerEntity entityToCreate = mapper.toEntity(draft);
        ManufacturerEntity createdEntity = jpaRepository.save(entityToCreate);

        return mapper.toDomain(createdEntity);
    }
}
