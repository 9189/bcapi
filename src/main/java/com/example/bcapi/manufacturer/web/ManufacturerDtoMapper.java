package com.example.bcapi.manufacturer.web;

import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.model.ManufacturerCreateRequestDto;
import com.example.bcapi.model.ManufacturerDto;
import com.example.bcapi.model.ManufacturerPageDto;
import com.example.bcapi.model.ManufacturerUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerDtoMapper {

    public ManufacturerDto toDto(Manufacturer manufacturer) {
        return new ManufacturerDto(
                manufacturer.id(),
                manufacturer.name(),
                manufacturer.originCountry()
        )
                .createdAt(manufacturer.createdAt())
                .updatedAt(manufacturer.updatedAt());
    }

    public ManufacturerPageDto toDto(Page<Manufacturer> page) {
        return new ManufacturerPageDto()
                .items(page.items().stream().map(this::toDto).toList())
                .page(page.page())
                .size(page.size())
                .hasMore(page.hasMore());
    }

    public ManufacturerDraft toDomain(ManufacturerCreateRequestDto dto) {
        return new ManufacturerDraft(
                dto.getName(),
                dto.getOriginCountry()
        );
    }

    public ManufacturerDraft toDomain(ManufacturerUpdateRequestDto dto) {
        return new ManufacturerDraft(
                dto.getName(),
                dto.getOriginCountry()
        );
    }
}
