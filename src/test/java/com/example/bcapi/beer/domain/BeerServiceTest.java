package com.example.bcapi.beer.domain;

import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.Manufacturer;
import com.example.bcapi.manufacturer.domain.ManufacturerNotFoundException;
import com.example.bcapi.manufacturer.domain.ManufacturerService;
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

class BeerServiceTest {

    private final BeerRepository beerRepository = mock(BeerRepository.class);
    private final ManufacturerService manufacturerService = mock(ManufacturerService.class);
    private final BeerService beerService = new BeerService(beerRepository, manufacturerService);

    private final Manufacturer manufacturer = new Manufacturer(UUID.randomUUID(), "Heineken", "NL", now(), now());

    @Test
    void create_validDraft_returnsPersistedBeer() {
        var draft = new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer.id());
        var expected = new Beer(UUID.randomUUID(), "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, now(), now());
        when(manufacturerService.findById(manufacturer.id())).thenReturn(manufacturer);
        when(beerRepository.create(any())).thenReturn(expected);

        var result = beerService.create(draft);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void create_unknownManufacturer_throwsNotFoundException() {
        var draft = new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer.id());
        when(manufacturerService.findById(manufacturer.id())).thenThrow(new ManufacturerNotFoundException(manufacturer.id()));

        assertThatThrownBy(() -> beerService.create(draft))
                .isInstanceOf(ManufacturerNotFoundException.class);
    }

    @Test
    void create_repositoryFails_throwsException() {
        var draft = new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer.id());
        when(manufacturerService.findById(manufacturer.id())).thenReturn(manufacturer);
        when(beerRepository.create(any())).thenThrow(new RuntimeException("persistence failure"));

        assertThatThrownBy(() -> beerService.create(draft))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("persistence failure");
    }

    @Test
    void update_existingBeer_returnsUpdated() {
        var id = UUID.randomUUID();
        var draft = new BeerDraft("Zipfer Urquell Premium", BeerType.LAGER, 5.2, "An upgraded lager", manufacturer.id());
        var existing = new Beer(id, "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, now(), now());
        var expected = new Beer(id, draft.name(), draft.type(), draft.abv(), draft.description(), manufacturer, existing.createdAt(), now());
        when(beerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(manufacturerService.findById(manufacturer.id())).thenReturn(manufacturer);
        when(beerRepository.update(any())).thenReturn(expected);

        var result = beerService.update(id, draft);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void update_withNewManufacturer_resolvesManufacturer() {
        var id = UUID.randomUUID();
        var newManufacturer = new Manufacturer(UUID.randomUUID(), "Estrella Galicia", "ES", now(), now());
        var draft = new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", newManufacturer.id());
        var existing = new Beer(id, "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, now(), now());
        var expected = new Beer(id, draft.name(), draft.type(), draft.abv(), draft.description(), newManufacturer, existing.createdAt(), now());
        when(beerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(manufacturerService.findById(newManufacturer.id())).thenReturn(newManufacturer);
        when(beerRepository.update(any())).thenReturn(expected);

        var result = beerService.update(id, draft);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void update_unknownId_throwsNotFoundException() {
        var id = UUID.randomUUID();
        var draft = new BeerDraft("Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer.id());
        when(beerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beerService.update(id, draft))
                .isInstanceOf(BeerNotFoundException.class);
    }

    @Test
    void findById_existingId_returnsBeer() {
        var beer = new Beer(UUID.randomUUID(), "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, now(), now());
        when(beerRepository.findById(beer.id())).thenReturn(Optional.of(beer));

        var result = beerService.findById(beer.id());

        assertThat(result).isEqualTo(beer);
    }

    @Test
    void findById_unknownId_throwsNotFoundException() {
        var id = UUID.randomUUID();
        when(beerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beerService.findById(id))
                .isInstanceOf(BeerNotFoundException.class);
    }

    @Test
    void findAll_returnsPage() {
        var query = new BeerQuery(0, 20, "name", SortDirection.ASC, "Zipfer");
        var beer = new Beer(UUID.randomUUID(), "Zipfer Urquell", BeerType.LAGER, 5.0, "A classic lager", manufacturer, now(), now());
        var page = new Page<>(List.of(beer), 0, 20, false);
        when(beerRepository.findAll(query)).thenReturn(page);

        var result = beerService.findAll(query);

        assertThat(result).isEqualTo(page);
    }

    @Test
    void delete_delegatesToRepository() {
        var id = UUID.randomUUID();

        beerService.delete(id);

        verify(beerRepository).delete(id);
    }
}
