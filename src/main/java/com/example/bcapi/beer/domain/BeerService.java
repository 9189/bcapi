package com.example.bcapi.beer.domain;

import com.example.bcapi.common.domain.Page;
import com.example.bcapi.manufacturer.domain.ManufacturerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BeerService {

    private final BeerRepository beerRepository;
    private final ManufacturerService manufacturerService;

    public BeerService(BeerRepository beerRepository, ManufacturerService manufacturerService) {
        this.beerRepository = beerRepository;
        this.manufacturerService = manufacturerService;
    }

    public Beer create(BeerDraft draft) {
        manufacturerService.findById(draft.manufacturerId());
        return beerRepository.create(draft);
    }

    public Beer update(UUID id, BeerDraft draft) {
        Beer existing = findById(id);
        var manufacturer = manufacturerService.findById(draft.manufacturerId());
        Beer beerToSave = new Beer(
                existing.id(),
                draft.name(),
                draft.type(),
                draft.abv(),
                draft.description(),
                manufacturer,
                existing.createdAt(),
                existing.updatedAt()
        );

        return beerRepository.update(beerToSave);
    }

    public void delete(UUID id) {
        beerRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public Beer findById(UUID id) {
        return beerRepository.findById(id).orElseThrow(() -> new BeerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Beer> findAll(BeerQuery query) {
        return beerRepository.findAll(query);
    }
}
