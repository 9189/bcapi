package com.example.bcapi.security;

import com.example.bcapi.beer.persistence.JpaBackedBeerRepository;
import com.example.bcapi.manufacturer.persistence.JpaBackedManufacturerRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Component
public class OwnershipPermissionEvaluator implements PermissionEvaluator {

    private final JpaBackedManufacturerRepository manufacturerRepository;
    private final JpaBackedBeerRepository beerRepository;

    public OwnershipPermissionEvaluator(
            JpaBackedManufacturerRepository manufacturerRepository,
            JpaBackedBeerRepository beerRepository
    ) {
        this.manufacturerRepository = manufacturerRepository;
        this.beerRepository = beerRepository;
    }

    @Override
    public boolean hasPermission(
            @NonNull Authentication auth,
            @NonNull Object targetDomainObject,
            @NonNull Object permission
    ) {
        return false;
    }

    @Override
    public boolean hasPermission(
            @NonNull Authentication auth,
            @NonNull Serializable targetId,
            @NonNull String targetType,
            @NonNull Object permission
    ) {
        if (!(targetId instanceof UUID id)) {
            return false;
        }
        if (isAdmin(auth)) {
            return true;
        }
        String username = auth.getName();
        return switch (targetType) {
            case "Manufacturer" -> manufacturerRepository.existsByIdAndOwner(id, username);
            case "Beer" -> beerRepository.existsByIdAndManufacturerOwner(id, username);
            default -> false;
        };
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
