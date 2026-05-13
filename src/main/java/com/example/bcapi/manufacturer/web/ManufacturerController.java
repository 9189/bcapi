package com.example.bcapi.manufacturer.web;

import com.example.bcapi.api.ManufacturersApi;
import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.manufacturer.domain.ManufacturerQuery;
import com.example.bcapi.manufacturer.domain.ManufacturerService;
import com.example.bcapi.model.ManufacturerCreateRequestDto;
import com.example.bcapi.model.ManufacturerDto;
import com.example.bcapi.model.ManufacturerPageDto;
import com.example.bcapi.model.ManufacturerUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

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

    @Override
    public ResponseEntity<ManufacturerDto> updateManufacturer(UUID id, ManufacturerUpdateRequestDto manufacturerUpdateRequestDto) {
        ManufacturerDraft manufacturerToUpdate = mapper.toDomain(manufacturerUpdateRequestDto);
        Manufacturer result = manufacturerService.update(id, manufacturerToUpdate);
        ManufacturerDto responseDto = mapper.toDto(result);

        return ResponseEntity.ok().body(responseDto);
    }

    @Override
    public ResponseEntity<Void> deleteManufacturer(UUID id) {
        manufacturerService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ManufacturerDto> getManufacturer(UUID id) {
        Manufacturer manufacturer = manufacturerService.findById(id);
        ManufacturerDto responseDto = mapper.toDto(manufacturer);

        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<ManufacturerPageDto> getManufacturers(Integer page, Integer size) {
        ManufacturerQuery query = new ManufacturerQuery(page, size);
        Page<Manufacturer> manufacturerPage = manufacturerService.findAll(query);
        ManufacturerPageDto responseDto = mapper.toDto(manufacturerPage);

        return ResponseEntity.ok(responseDto);
    }
}
