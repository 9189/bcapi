package com.example.bcapi.beer.persistence;

import com.example.bcapi.beer.domain.Beer;
import com.example.bcapi.beer.domain.BeerDraft;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.persistence.ManufacturerEntity;
import com.example.bcapi.manufacturer.persistence.ManufacturerEntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class BeerEntityMapperTest {

    private final BeerEntityMapper mapper = new BeerEntityMapper(new ManufacturerEntityMapper());

    private final UUID manufacturerId = UUID.randomUUID();
    private final ManufacturerEntity manufacturerEntity = new ManufacturerEntity(
            manufacturerId, "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now()
    );

    @Test
    void toDomain_allFieldsMapped() {
        var entity = new BeerEntity(UUID.randomUUID(), "Zipfer Urquell", "Lager", 5.0, "A classic lager", manufacturerEntity, OffsetDateTime.now(), OffsetDateTime.now());

        var result = mapper.toDomain(entity);

        assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(entity.getId());
            softly.assertThat(result.name()).isEqualTo(entity.getName());
            softly.assertThat(result.type()).isEqualTo(entity.getType());
            softly.assertThat(result.abv()).isEqualTo(entity.getAbv());
            softly.assertThat(result.description()).isEqualTo(entity.getDescription());
            softly.assertThat(result.manufacturer().id()).isEqualTo(manufacturerId);
            softly.assertThat(result.createdAt()).isEqualTo(entity.getCreatedAt());
            softly.assertThat(result.updatedAt()).isEqualTo(entity.getUpdatedAt());
        });
    }

    @Test
    void toEntity_allFieldsMapped() {
        var beer = new Beer(UUID.randomUUID(), "Zipfer Urquell", "Lager", 5.0, "A classic lager",
                new Manufacturer(manufacturerId, "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now()),
                OffsetDateTime.now(), OffsetDateTime.now());

        var result = mapper.toEntity(beer, manufacturerEntity);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(beer.id());
            softly.assertThat(result.getName()).isEqualTo(beer.name());
            softly.assertThat(result.getType()).isEqualTo(beer.type());
            softly.assertThat(result.getAbv()).isEqualTo(beer.abv());
            softly.assertThat(result.getDescription()).isEqualTo(beer.description());
            softly.assertThat(result.getManufacturer().getId()).isEqualTo(manufacturerId);
            softly.assertThat(result.getCreatedAt()).isEqualTo(beer.createdAt());
            softly.assertThat(result.getUpdatedAt()).isEqualTo(beer.updatedAt());
        });
    }

    @Test
    void draftToEntity_allFieldsMapped() {
        var draft = new BeerDraft("Zipfer Urquell", "Lager", 5.0, "A classic lager", manufacturerId);

        var result = mapper.toEntity(draft, manufacturerEntity);

        assertSoftly(softly -> {
            softly.assertThat(result.getName()).isEqualTo(draft.name());
            softly.assertThat(result.getType()).isEqualTo(draft.type());
            softly.assertThat(result.getAbv()).isEqualTo(draft.abv());
            softly.assertThat(result.getDescription()).isEqualTo(draft.description());
            softly.assertThat(result.getManufacturer().getId()).isEqualTo(manufacturerId);
            softly.assertThat(result.getId()).isNull();
            softly.assertThat(result.getCreatedAt()).isNull();
            softly.assertThat(result.getUpdatedAt()).isNull();
        });
    }

    @Test
    void toDomain_toEntity_roundtrip() {
        var entity = new BeerEntity(UUID.randomUUID(), "Zipfer Urquell", "Lager", 5.0, "A classic lager", manufacturerEntity, OffsetDateTime.now(), OffsetDateTime.now());

        var domain = mapper.toDomain(entity);
        var result = mapper.toEntity(domain, manufacturerEntity);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(entity.getId());
            softly.assertThat(result.getName()).isEqualTo(entity.getName());
            softly.assertThat(result.getType()).isEqualTo(entity.getType());
            softly.assertThat(result.getAbv()).isEqualTo(entity.getAbv());
            softly.assertThat(result.getDescription()).isEqualTo(entity.getDescription());
            softly.assertThat(result.getManufacturer().getId()).isEqualTo(manufacturerId);
            softly.assertThat(result.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            softly.assertThat(result.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        });
    }

    @Test
    void toPage_mapsItemsPageSizeAndHasMore() {
        var entity = new BeerEntity(UUID.randomUUID(), "Zipfer Urquell", "Lager", 5.0, "A classic lager", manufacturerEntity, OffsetDateTime.now(), OffsetDateTime.now());
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 2);

        var result = mapper.toDomain(springPage);

        assertSoftly(softly -> {
            softly.assertThat(result.items()).hasSize(1);
            softly.assertThat(result.items().getFirst()).isEqualTo(mapper.toDomain(entity));
            softly.assertThat(result.page()).isZero();
            softly.assertThat(result.size()).isEqualTo(1);
            softly.assertThat(result.hasMore()).isTrue();
        });
    }

    @Test
    void toPage_lastPage_hasMoreIsFalse() {
        var entity = new BeerEntity(UUID.randomUUID(), "Zipfer Urquell", "Lager", 5.0, "A classic lager", manufacturerEntity, OffsetDateTime.now(), OffsetDateTime.now());
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);

        var result = mapper.toDomain(springPage);

        assertThat(result.hasMore()).isFalse();
    }
}
