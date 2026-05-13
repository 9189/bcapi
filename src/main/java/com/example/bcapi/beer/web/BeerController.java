package com.example.bcapi.beer.web;

import com.example.bcapi.api.BeersApi;
import com.example.bcapi.beer.domain.BeerQuery;
import com.example.bcapi.beer.domain.BeerService;
import com.example.bcapi.model.BeerCreateRequestDto;
import com.example.bcapi.model.BeerDto;
import com.example.bcapi.model.BeerPageDto;
import com.example.bcapi.model.BeerUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class BeerController implements BeersApi {

    private final BeerService beerService;
    private final BeerDtoMapper mapper;

    public BeerController(BeerService beerService, BeerDtoMapper mapper) {
        this.beerService = beerService;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<BeerDto> createBeer(BeerCreateRequestDto request) {
        var draft = mapper.toDomain(request);
        var beer = beerService.create(draft);
        var location = URI.create("/api/beers/%s".formatted(beer.id()));

        return ResponseEntity.created(location).body(mapper.toDto(beer));
    }

    @Override
    public ResponseEntity<BeerDto> getBeer(UUID id) {
        return ResponseEntity.ok(mapper.toDto(beerService.findById(id)));
    }

    @Override
    public ResponseEntity<BeerDto> updateBeer(UUID id, BeerUpdateRequestDto request) {
        var draft = mapper.toDomain(request);

        return ResponseEntity.ok(mapper.toDto(beerService.update(id, draft)));
    }

    @Override
    public ResponseEntity<Void> deleteBeer(UUID id) {
        beerService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BeerPageDto> getBeers(Integer page, Integer size) {
        var result = beerService.findAll(new BeerQuery(page, size));

        return ResponseEntity.ok(mapper.toDto(result));
    }
}
