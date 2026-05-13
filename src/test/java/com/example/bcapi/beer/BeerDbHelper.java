package com.example.bcapi.beer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static java.time.OffsetDateTime.now;

@Component
public class BeerDbHelper {

    private final JdbcTemplate jdbcTemplate;

    public BeerDbHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> findById(UUID id) {
        return jdbcTemplate.queryForMap("SELECT * FROM beers WHERE id = ?", id);
    }

    public int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM beers", Integer.class);
    }

    public UUID insert(String name, String type, double abv, String description, UUID manufacturerId) {
        var id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO beers (id, name, type, abv, description, manufacturer_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, name, type, abv, description, manufacturerId, now(), now()
        );
        return id;
    }
}
