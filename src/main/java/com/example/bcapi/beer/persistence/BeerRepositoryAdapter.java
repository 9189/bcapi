package com.example.bcapi.beer.persistence;

import com.example.bcapi.beer.domain.Beer;
import com.example.bcapi.beer.domain.BeerDraft;
import com.example.bcapi.beer.domain.BeerQuery;
import com.example.bcapi.beer.domain.BeerRepository;
import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.persistence.ManufacturerEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BeerRepositoryAdapter implements BeerRepository {

    private final JpaBackedBeerRepository jpaRepository;
    private final BeerEntityMapper mapper;
    private final EntityManager entityManager;

    public BeerRepositoryAdapter(JpaBackedBeerRepository jpaRepository, BeerEntityMapper mapper, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Beer create(BeerDraft draft) {
        var manufacturer = entityManager.getReference(ManufacturerEntity.class, draft.manufacturerId());
        var entityToCreate = mapper.toEntity(draft, manufacturer);
        var createdEntity = jpaRepository.save(entityToCreate);

        return mapper.toDomain(createdEntity);
    }

    @Override
    public Beer update(Beer beer) {
        var manufacturer = entityManager.getReference(ManufacturerEntity.class, beer.manufacturer().id());
        var entityToUpdate = mapper.toEntity(beer, manufacturer);
        var updatedEntity = jpaRepository.save(entityToUpdate);

        return mapper.toDomain(updatedEntity);
    }

    @Override
    public Optional<Beer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Beer> findAll(BeerQuery query) {
        var pageRequest = PageRequest.of(query.page(), query.size(), Sort.by("name").ascending());

        return mapper.toDomain(jpaRepository.findAll(pageRequest));
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
