package com.example.bcapi.beer.web;

import com.example.bcapi.beer.domain.Beer;
import com.example.bcapi.beer.domain.BeerDraft;
import com.example.bcapi.beer.domain.BeerType;
import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.web.ManufacturerDtoMapper;
import com.example.bcapi.model.BeerCreateRequestDto;
import com.example.bcapi.model.BeerUpdateRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.example.bcapi.beer.domain.InvalidBeerTypeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class BeerDtoMapperTest {

    private final BeerDtoMapper mapper = new BeerDtoMapper(new ManufacturerDtoMapper());

    private final Manufacturer manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());
    private final Beer beer = new Beer(UUID.randomUUID(), "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, OffsetDateTime.now(), OffsetDateTime.now());

    @Test
    void toDto_allFieldsMapped() {
        var result = mapper.toDto(beer);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(beer.id());
            softly.assertThat(result.getName()).isEqualTo(beer.name());
            softly.assertThat(result.getType()).isEqualTo(beer.type().name());
            softly.assertThat(result.getAbv()).isEqualTo(beer.abv());
            softly.assertThat(result.getDescription()).isEqualTo(beer.description());
            softly.assertThat(result.getManufacturer().getId()).isEqualTo(manufacturer.id());
            softly.assertThat(result.getCreatedAt()).isEqualTo(beer.createdAt());
            softly.assertThat(result.getUpdatedAt()).isEqualTo(beer.updatedAt());
        });
    }

    @Test
    void pageToDto_allFieldsMapped() {
        var page = new Page<>(List.of(beer), 0, 20, false);

        var result = mapper.toDto(page);

        assertSoftly(softly -> {
            softly.assertThat(result.getItems()).hasSize(1);
            softly.assertThat(result.getItems().getFirst().getId()).isEqualTo(beer.id());
            softly.assertThat(result.getPage()).isEqualTo(page.page());
            softly.assertThat(result.getSize()).isEqualTo(page.size());
            softly.assertThat(result.getHasMore()).isEqualTo(page.hasMore());
        });
    }

    @Test
    void createRequestToDomain_allFieldsMapped() {
        var manufacturerId = UUID.randomUUID();
        var request = new BeerCreateRequestDto("Zipfer Urquell", "LAGER", 5.0, manufacturerId)
                .description("A classic lager");

        var result = mapper.toDomain(request);

        assertThat(result).isEqualTo(new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturerId));
    }

    @ParameterizedTest
    @EnumSource(BeerType.class)
    void createRequestToDomain_allBeerTypesParsed(BeerType beerType) {
        var request = new BeerCreateRequestDto("Zipfer Urquell", beerType.name(), 5.0, UUID.randomUUID());

        var result = mapper.toDomain(request);

        assertThat(result.type()).isEqualTo(beerType);
    }

    @Test
    void createRequestToDomain_invalidBeerType_throwsException() {
        var request = new BeerCreateRequestDto("Zipfer Urquell", "UNKNOWN_TYPE", 5.0, UUID.randomUUID());

        assertThatThrownBy(() -> mapper.toDomain(request)).isInstanceOf(InvalidBeerTypeException.class);
    }

    @Test
    void updateRequestToDomain_allFieldsMapped() {
        var manufacturerId = UUID.randomUUID();
        var request = new BeerUpdateRequestDto("Zipfer Urquell", "LAGER", 5.0, "A classic lager", manufacturerId);

        var result = mapper.toDomain(request);

        assertThat(result).isEqualTo(new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturerId));
    }
}
