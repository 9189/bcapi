package com.example.bcapi.manufacturer;

import com.example.bcapi.common.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
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

    @Test
    void post_invalidCountryCode_returnsUnprocessableEntityWithProblemDetail() {
        var result = restTestClient.post().uri("/api/manufacturers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Fake Brewery",
                          "originCountry": "XX"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        assertSoftly(softly -> {
            var body = Objects.requireNonNull(result.getResponseBody());
            softly.assertThat(body.get("status")).isEqualTo(422);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
            softly.assertThat(db.count()).isZero();
        });
    }

    @Test
    void post_missingRequiredField_returnsBadRequestWithProblemDetail() {
        var result = restTestClient.post().uri("/api/manufacturers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "originCountry": "ES"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        assertSoftly(softly -> {
            var body = Objects.requireNonNull(result.getResponseBody());
            softly.assertThat(body.get("status")).isEqualTo(400);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
            softly.assertThat(db.count()).isZero();
        });
    }

    @Test
    void get_existingId_returnsManufacturer() {
        var id = db.insert("Estrella Galicia", "ES");

        var result = restTestClient.get().uri("/api/manufacturers/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("id")).isEqualTo(id.toString());
            softly.assertThat(body.get("name")).isEqualTo("Estrella Galicia");
            softly.assertThat(body.get("originCountry")).isEqualTo("ES");
        });
    }

    @Test
    void get_unknownId_returnsNotFoundWithProblemDetail() {
        var result = restTestClient.get().uri("/api/manufacturers/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(404);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
        });
    }

    @Test
    void put_validRequest_returnsUpdatedAndPersists() {
        var id = db.insert("Heineken", "NL");

        var result = restTestClient.put().uri("/api/manufacturers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Heineken International",
                          "originCountry": "NL"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var row = db.findById(id);
        assertSoftly(softly -> {
            softly.assertThat(result.getResponseBody()).isNotNull();
            softly.assertThat(row.get("name")).isEqualTo("Heineken International");
            softly.assertThat(row.get("origin_country")).isEqualTo("NL");
        });
    }

    @Test
    void delete_existingId_returnsNoContentAndRemoves() {
        var id = db.insert("Heineken", "NL");

        restTestClient.delete().uri("/api/manufacturers/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        assertSoftly(softly -> softly.assertThat(db.count()).isZero());
    }

    @Test
    void getAll_returnsPage() {
        db.insert("Estrella Galicia", "ES");
        db.insert("Heineken", "NL");

        var result = restTestClient.get().uri("/api/manufacturers?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat((List<?>) body.get("items")).hasSize(2);
            softly.assertThat(body.get("page")).isEqualTo(0);
            softly.assertThat(body.get("size")).isEqualTo(10);
            softly.assertThat(body.get("hasMore")).isEqualTo(false);
        });
    }
}
