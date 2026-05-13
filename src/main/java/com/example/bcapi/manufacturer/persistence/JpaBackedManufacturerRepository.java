package com.example.bcapi.manufacturer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaBackedManufacturerRepository extends JpaRepository<ManufacturerEntity, UUID> {
}
