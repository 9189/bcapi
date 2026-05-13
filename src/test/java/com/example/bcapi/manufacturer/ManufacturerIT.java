package com.example.bcapi.manufacturer;

import com.example.bcapi.common.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@IntegrationTest
class ManufacturerIT {

    @Autowired
    RestTestClient restTestClient;

    @Autowired
    ManufacturerDbHelper db;

    @Test
    void post_validRequest_returnsCreatedWithLocationAndPersists() {
        var result = restTestClient.post().uri("/api/manufacturers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Estrella Galicia",
                          "originCountry": "ES"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult();

        var location = result.getResponseHeaders().getLocation();
        var id = UUID.fromString((String) Objects.requireNonNull(result.getResponseBody()).get("id"));
        var row = db.findById(id);

        assertSoftly(softly -> {
            softly.assertThat(location).isNotNull();
            softly.assertThat(location.getPath()).startsWith("/api/manufacturers/");
            softly.assertThat(row.get("name")).isEqualTo("Estrella Galicia");
            softly.assertThat(row.get("origin_country")).isEqualTo("ES");
        });
    }
}
