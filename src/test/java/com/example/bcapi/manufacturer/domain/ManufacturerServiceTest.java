package com.example.bcapi.manufacturer.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManufacturerServiceTest {

    private final ManufacturerRepository manufacturerRepository = mock(ManufacturerRepository.class);
    private final ManufacturerService manufacturerService = new ManufacturerService(manufacturerRepository);

    @Test
    void create_validDraft_returnsPersistedManufacturer() {
        var draft = new ManufacturerDraft("Heineken", "NL");
        var expected = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", now(), now());
        when(manufacturerRepository.create(any())).thenReturn(expected);

        var result = manufacturerService.create(draft);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_repositoryFails_throwsException() {
        var draft = new ManufacturerDraft("Heineken", "NL");
        when(manufacturerRepository.create(any())).thenThrow(new RuntimeException("persistence failure"));

        assertThatThrownBy(() -> manufacturerService.create(draft))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("persistence failure");
    }
}
