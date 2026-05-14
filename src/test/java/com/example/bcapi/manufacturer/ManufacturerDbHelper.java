package com.example.bcapi.manufacturer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static java.time.OffsetDateTime.now;

@Component
public class ManufacturerDbHelper {

    private final JdbcTemplate jdbcTemplate;

    public ManufacturerDbHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> findById(UUID id) {
        return jdbcTemplate.queryForMap("SELECT * FROM manufacturers WHERE id = ?", id);
    }

    public int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM manufacturers", Integer.class);
    }

    public UUID insert(String name, String originCountry) {
        var id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO manufacturers (id, name, origin_country, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                id, name, originCountry, now(), now()
        );
        return id;
    }

    public UUID insertWithOwner(String name, String originCountry, String owner) {
        var id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO manufacturers (id, name, origin_country, owner, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                id, name, originCountry, owner, now(), now()
        );
        return id;
    }
}
