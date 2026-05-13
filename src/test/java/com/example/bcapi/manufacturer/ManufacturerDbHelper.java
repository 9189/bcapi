package com.example.bcapi.manufacturer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
class ManufacturerDbHelper {

    private final JdbcTemplate jdbcTemplate;

    ManufacturerDbHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Map<String, Object> findById(UUID id) {
        return jdbcTemplate.queryForMap("SELECT * FROM manufacturers WHERE id = ?", id);
    }
}
