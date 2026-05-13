package com.example.bcapi.manufacturer.persistence;

import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import org.springframework.stereotype.Component;

@Component
class ManufacturerEntityMapper {

    Manufacturer toDomain(ManufacturerEntity entity) {
        return new Manufacturer(
                entity.getId(),
                entity.getName(),
                entity.getOriginCountry(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    Page<Manufacturer> toDomain(org.springframework.data.domain.Page<ManufacturerEntity> page) {
        return new Page<>(
                page.map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.hasNext()
        );
    }

    ManufacturerEntity toEntity(Manufacturer manufacturer) {
        return new ManufacturerEntity(
                manufacturer.id(),
                manufacturer.name(),
                manufacturer.originCountry(),
                manufacturer.createdAt(),
                manufacturer.updatedAt()
        );
    }

    ManufacturerEntity toEntity(ManufacturerDraft draft) {
        return new ManufacturerEntity(
                draft.name(),
                draft.originCountry()
        );
    }
}
