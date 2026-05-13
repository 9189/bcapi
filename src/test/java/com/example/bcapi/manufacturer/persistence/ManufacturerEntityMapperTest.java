package com.example.bcapi.manufacturer.persistence;

import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerDraft;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class ManufacturerEntityMapperTest {

    private final ManufacturerEntityMapper mapper = new ManufacturerEntityMapper();

    @Test
    void toDomain_allFieldsMapped() {
        var entity = new ManufacturerEntity(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());
        var expected = new Manufacturer(entity.getId(), entity.getName(), entity.getOriginCountry(), entity.getCreatedAt(), entity.getUpdatedAt());

        var result = mapper.toDomain(entity);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void toEntity_allFieldsMapped() {
        var manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());

        var result = mapper.toEntity(manufacturer);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(manufacturer.id());
            softly.assertThat(result.getName()).isEqualTo(manufacturer.name());
            softly.assertThat(result.getOriginCountry()).isEqualTo(manufacturer.originCountry());
            softly.assertThat(result.getCreatedAt()).isEqualTo(manufacturer.createdAt());
            softly.assertThat(result.getUpdatedAt()).isEqualTo(manufacturer.updatedAt());
        });
    }

    @Test
    void draftToEntity_allFieldsMapped() {
        var draft = new ManufacturerDraft("Heineken", "NL");

        var result = mapper.toEntity(draft);

        assertSoftly(softly -> {
            softly.assertThat(result.getName()).isEqualTo(draft.name());
            softly.assertThat(result.getOriginCountry()).isEqualTo(draft.originCountry());
            softly.assertThat(result.getId()).isNull();
            softly.assertThat(result.getCreatedAt()).isNull();
            softly.assertThat(result.getUpdatedAt()).isNull();
        });
    }

    @Test
    void toDomain_toEntity_roundtrip() {
        var original = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());

        assertThat(mapper.toDomain(mapper.toEntity(original))).isEqualTo(original);
    }

    @Test
    void toPage_mapsItemsPageSizeAndHasMore() {
        var entity = new ManufacturerEntity(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());
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
        var entity = new ManufacturerEntity(UUID.randomUUID(), "Heineken", "NL", OffsetDateTime.now(), OffsetDateTime.now());
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);

        var result = mapper.toDomain(springPage);

        assertThat(result.hasMore()).isFalse();
    }
}
