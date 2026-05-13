package com.example.bcapi.manufacturer.web;

import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import com.example.bcapi.model.ManufacturerCreateRequestDto;
import com.example.bcapi.model.ManufacturerUpdateRequestDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ManufacturerDtoMapperTest {

    private final ManufacturerDtoMapper mapper = new ManufacturerDtoMapper();

    @Test
    void toDto_allFieldsMapped() {
        var manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());

        var result = mapper.toDto(manufacturer);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(manufacturer.id());
            softly.assertThat(result.getName()).isEqualTo(manufacturer.name());
            softly.assertThat(result.getOriginCountry()).isEqualTo(manufacturer.originCountry());
            softly.assertThat(result.getCreatedAt()).isEqualTo(manufacturer.createdAt());
            softly.assertThat(result.getUpdatedAt()).isEqualTo(manufacturer.updatedAt());
        });
    }

    @Test
    void createRequestToDomain_allFieldsMapped() {
        var request = new ManufacturerCreateRequestDto("Heineken", "NL");

        var result = mapper.toDomain(request);

        assertThat(result).isEqualTo(new ManufacturerDraft("Heineken", "NL"));
    }

    @Test
    void updateRequestToDomain_allFieldsMapped() {
        var request = new ManufacturerUpdateRequestDto("Heineken", "NL");

        var result = mapper.toDomain(request);

        assertThat(result).isEqualTo(new ManufacturerDraft("Heineken", "NL"));
    }
}
