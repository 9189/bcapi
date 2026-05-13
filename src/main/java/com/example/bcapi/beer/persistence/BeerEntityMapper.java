package com.example.bcapi.beer.persistence;

import com.example.bcapi.beer.domain.Beer;
import com.example.bcapi.beer.domain.BeerDraft;
import com.example.bcapi.beer.domain.BeerType;
import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.persistence.ManufacturerEntity;
import com.example.bcapi.manufacturer.persistence.ManufacturerEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class BeerEntityMapper {

    private final ManufacturerEntityMapper manufacturerEntityMapper;

    public BeerEntityMapper(ManufacturerEntityMapper manufacturerEntityMapper) {
        this.manufacturerEntityMapper = manufacturerEntityMapper;
    }

    public Beer toDomain(BeerEntity entity) {
        return new Beer(
                entity.getId(),
                entity.getName(),
                BeerType.valueOf(entity.getType()),
                entity.getAbv(),
                entity.getDescription(),
                manufacturerEntityMapper.toDomain(entity.getManufacturer()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public BeerEntity toEntity(BeerDraft draft, ManufacturerEntity manufacturer) {
        return new BeerEntity(
                draft.name(),
                draft.type().name(),
                draft.abv(),
                draft.description(),
                manufacturer
        );
    }

    public BeerEntity toEntity(Beer beer, ManufacturerEntity manufacturer) {
        return new BeerEntity(
                beer.id(),
                beer.name(),
                beer.type().name(),
                beer.abv(),
                beer.description(),
                manufacturer,
                beer.createdAt(),
                beer.updatedAt()
        );
    }

    public Page<Beer> toDomain(org.springframework.data.domain.Page<BeerEntity> page) {
        return new Page<>(
                page.map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.hasNext()
        );
    }
}
