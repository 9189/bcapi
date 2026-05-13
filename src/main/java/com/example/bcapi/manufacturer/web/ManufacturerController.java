package com.example.bcapi.manufacturer.web;

import com.example.bcapi.api.ManufacturersApi;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.manufacturer.domain.ManufacturerService;
import com.example.bcapi.model.ManufacturerCreateRequestDto;
import com.example.bcapi.model.ManufacturerDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ManufacturerController implements ManufacturersApi {

    private final ManufacturerDtoMapper mapper;
    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerDtoMapper mapper, ManufacturerService manufacturerService) {
        this.mapper = mapper;
        this.manufacturerService = manufacturerService;
    }

    @Override
    public ResponseEntity<ManufacturerDto> createManufacturer(ManufacturerCreateRequestDto manufacturerCreateRequestDto) {
        ManufacturerDraft draft = mapper.toDomain(manufacturerCreateRequestDto);
        Manufacturer createdManufacturer = manufacturerService.create(draft);
        ManufacturerDto responseDto = mapper.toDto(createdManufacturer);
        URI responseLocation = URI.create("/api/manufacturers/%s".formatted(createdManufacturer.id()));

        return ResponseEntity.created(responseLocation).body(responseDto);
    }
}
