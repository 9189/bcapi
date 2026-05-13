package com.example.bcapi.manufacturer.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "manufacturers")
class ManufacturerEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(name = "origin_country")
    private String originCountry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected ManufacturerEntity() {}

    ManufacturerEntity(String name, String originCountry) {
        this.name = name;
        this.originCountry = originCountry;
    }

    ManufacturerEntity(UUID id, String name, String originCountry, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.originCountry = originCountry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    String getOriginCountry() { return originCountry; }
    OffsetDateTime getCreatedAt() { return createdAt; }
    OffsetDateTime getUpdatedAt() { return updatedAt; }
}
