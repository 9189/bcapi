package com.example.bcapi.manufacturer.persistence;

import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.manufacturer.domain.ManufacturerQuery;
import com.example.bcapi.manufacturer.domain.ManufacturerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        ManufacturerEntity entityToUpdate = mapper.toEntity(manufacturer);
        ManufacturerEntity updatedEntity = jpaRepository.save(entityToUpdate);

        return mapper.toDomain(updatedEntity);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Manufacturer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Manufacturer> findAll(ManufacturerQuery query) {
        Sort sort = Sort.by("name").ascending();
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), sort);
        var result = jpaRepository.findAll(pageRequest);

        return mapper.toDomain(result);
    }
}
