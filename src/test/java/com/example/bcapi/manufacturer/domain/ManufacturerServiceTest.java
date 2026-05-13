package com.example.bcapi.manufacturer.domain;

import com.example.bcapi.common.domain.Page;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManufacturerServiceTest {

    private final ManufacturerRepository manufacturerRepository = mock(ManufacturerRepository.class);
    private final CountryCodeValidator countryCodeValidator = new CountryCodeValidator();
    private final ManufacturerService manufacturerService = new ManufacturerService(manufacturerRepository, countryCodeValidator);

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

    @Test
    void create_invalidCountryCode_throwsValidationException() {
        var draft = new ManufacturerDraft("Heineken", "INVALID");

        assertThatThrownBy(() -> manufacturerService.create(draft)).isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void update_validManufacturer_returnsUpdated() {
        var id = UUID.randomUUID();
        var update = new ManufacturerDraft("Estrella Galicia", "ES");
        var existing = new Manufacturer(id, "Heineken", "NL", now(), now());
        var expected = new Manufacturer(existing.id(), update.name(), update.originCountry(), existing.createdAt(), now());
        when(manufacturerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(manufacturerRepository.update(any())).thenReturn(expected);

        var result = manufacturerService.update(id, update);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void update_invalidCountryCode_throwsValidationException() {
        var id = UUID.randomUUID();
        var manufacturer = new ManufacturerDraft( "Heineken", "INVALID");

        assertThatThrownBy(() -> manufacturerService.update(id, manufacturer))
                .isInstanceOf(InvalidCountryCodeException.class);
    }

    @Test
    void findById_existingId_returnsManufacturer() {
        var manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", now(), now());
        when(manufacturerRepository.findById(manufacturer.id())).thenReturn(Optional.of(manufacturer));

        var result = manufacturerService.findById(manufacturer.id());

        assertThat(result).isEqualTo(manufacturer);
    }

    @Test
    void findById_unknownId_throwsNotFoundException() {
        var id = UUID.randomUUID();
        when(manufacturerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manufacturerService.findById(id)).isInstanceOf(ManufacturerNotFoundException.class);
    }

    @Test
    void findAll_returnsPage() {
        var query = new ManufacturerQuery(0, 20);
        var manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", now(), now());
        var page = new Page<>(List.of(manufacturer), 0, 20, false);
        when(manufacturerRepository.findAll(query)).thenReturn(page);

        var result = manufacturerService.findAll(query);

        assertThat(result).isEqualTo(page);
    }

    @Test
    void delete_delegatesToRepository() {
        var id = UUID.randomUUID();

        manufacturerService.delete(id);

        verify(manufacturerRepository).delete(id);
    }
}
