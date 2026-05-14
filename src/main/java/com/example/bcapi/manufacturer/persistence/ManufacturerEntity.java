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
public class ManufacturerEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(updatable = false)
    private String owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected ManufacturerEntity() {}

    public ManufacturerEntity(String name, String originCountry) {
        this.name = name;
        this.originCountry = originCountry;
    }

    public ManufacturerEntity(UUID id, String name, String originCountry, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.originCountry = originCountry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getOriginCountry() { return originCountry; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
