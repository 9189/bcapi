package com.example.bcapi.manufacturer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static java.time.OffsetDateTime.now;

@Component
class ManufacturerDbHelper {

    private final JdbcTemplate jdbcTemplate;

    ManufacturerDbHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Map<String, Object> findById(UUID id) {
        return jdbcTemplate.queryForMap("SELECT * FROM manufacturers WHERE id = ?", id);
    }

    int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM manufacturers", Integer.class);
    }

    UUID insert(String name, String originCountry) {
        var id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO manufacturers (id, name, origin_country, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                id, name, originCountry, now(), now()
        );
        return id;
    }
}
