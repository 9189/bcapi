package com.example.bcapi.manufacturer.web;

import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.model.ManufacturerCreateRequestDto;
import com.example.bcapi.model.ManufacturerDto;
import com.example.bcapi.model.ManufacturerUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerDtoMapper {

    ManufacturerDto toDto(Manufacturer manufacturer) {
        return new ManufacturerDto(
                manufacturer.id(),
                manufacturer.name(),
                manufacturer.originCountry()
        )
                .createdAt(manufacturer.createdAt())
                .updatedAt(manufacturer.updatedAt());
    }

    ManufacturerDraft toDomain(ManufacturerCreateRequestDto request) {
        return new ManufacturerDraft(
                request.getName(),
                request.getOriginCountry()
        );
    }

    ManufacturerDraft toDomain(ManufacturerUpdateRequestDto request) {
        return new ManufacturerDraft(
                request.getName(),
                request.getOriginCountry()
        );
    }
}
