package com.example.bcapi.beer.persistence;

import com.example.bcapi.manufacturer.persistence.ManufacturerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "beers")
public class BeerEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    private String type;

    private double abv;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private ManufacturerEntity manufacturer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected BeerEntity() {
    }

    public BeerEntity(String name, String type, double abv, String description, ManufacturerEntity manufacturer) {
        this.name = name;
        this.type = type;
        this.abv = abv;
        this.description = description;
        this.manufacturer = manufacturer;
    }

    public BeerEntity(
            UUID id,
            String name,
            String type,
            double abv,
            String description,
            ManufacturerEntity manufacturer,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.abv = abv;
        this.description = description;
        this.manufacturer = manufacturer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getAbv() {
        return abv;
    }

    public String getDescription() {
        return description;
    }

    public ManufacturerEntity getManufacturer() {
        return manufacturer;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
