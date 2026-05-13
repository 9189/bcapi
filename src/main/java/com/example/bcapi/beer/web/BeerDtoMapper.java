package com.example.bcapi.beer.web;

import com.example.bcapi.beer.domain.Beer;
import com.example.bcapi.beer.domain.BeerDraft;
import com.example.bcapi.beer.domain.BeerType;
import com.example.bcapi.beer.domain.InvalidBeerTypeException;
import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.web.ManufacturerDtoMapper;
import com.example.bcapi.model.BeerCreateRequestDto;
import com.example.bcapi.model.BeerDto;
import com.example.bcapi.model.BeerPageDto;
import com.example.bcapi.model.BeerUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class BeerDtoMapper {

    private final ManufacturerDtoMapper manufacturerDtoMapper;

    public BeerDtoMapper(ManufacturerDtoMapper manufacturerDtoMapper) {
        this.manufacturerDtoMapper = manufacturerDtoMapper;
    }

    public BeerDto toDto(Beer beer) {
        return new BeerDto()
                .id(beer.id())
                .name(beer.name())
                .type(beer.type().name())
                .abv(beer.abv())
                .description(beer.description())
                .manufacturer(manufacturerDtoMapper.toDto(beer.manufacturer()))
                .createdAt(beer.createdAt())
                .updatedAt(beer.updatedAt());
    }

    public BeerPageDto toDto(Page<Beer> page) {
        return new BeerPageDto()
                .items(page.items().stream().map(this::toDto).toList())
                .page(page.page())
                .size(page.size())
                .hasMore(page.hasMore());
    }

    public BeerDraft toDomain(BeerCreateRequestDto request) {
        return new BeerDraft(
                request.getName(),
                parseBeerType(request.getType()),
                request.getAbv(),
                request.getDescription(),
                request.getManufacturerId()
        );
    }

    public BeerDraft toDomain(BeerUpdateRequestDto request) {
        return new BeerDraft(
                request.getName(),
                parseBeerType(request.getType()),
                request.getAbv(),
                request.getDescription(),
                request.getManufacturerId()
        );
    }

    private BeerType parseBeerType(String type) {
        try {
            return BeerType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidBeerTypeException(type);
        }
    }
}
