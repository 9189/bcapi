package com.example.bcapi.beer;

import com.example.bcapi.common.IntegrationTest;
import com.example.bcapi.manufacturer.ManufacturerDbHelper;
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
class BeerIT {

    @Autowired
    RestTestClient restTestClient;

    @Autowired
    BeerDbHelper db;

    @Autowired
    ManufacturerDbHelper manufacturerDb;

    @Test
    void post_validRequest_returnsCreatedWithLocationAndPersists() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");

        var result = restTestClient.post().uri("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Zipfer Urquell",
                          "type": "LAGER",
                          "abv": 4.8,
                          "description": "A classic lager",
                          "manufacturerId": "%s"
                        }
                        """.formatted(manufacturerId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult();

        var location = result.getResponseHeaders().getLocation();
        var id = UUID.fromString((String) Objects.requireNonNull(result.getResponseBody()).get("id"));
        var row = db.findById(id);

        assertSoftly(softly -> {
            softly.assertThat(location).isNotNull();
            softly.assertThat(location.getPath()).startsWith("/api/beers/");
            softly.assertThat(row.get("name")).isEqualTo("Zipfer Urquell");
            softly.assertThat(row.get("type")).isEqualTo("LAGER");
            softly.assertThat(row.get("manufacturer_id")).isEqualTo(manufacturerId);
        });
    }

    @Test
    void get_existingId_returnsBeer() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        var id = db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);

        var result = restTestClient.get().uri("/api/beers/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("id")).isEqualTo(id.toString());
            softly.assertThat(body.get("name")).isEqualTo("Zipfer Urquell");
            softly.assertThat(body.get("type")).isEqualTo("LAGER");
        });
    }

    @Test
    void put_validRequest_returnsUpdatedAndPersists() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        var id = db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);

        restTestClient.put().uri("/api/beers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Zipfer Urquell Premium",
                          "type": "LAGER",
                          "abv": 5.2,
                          "description": "An upgraded lager",
                          "manufacturerId": "%s"
                        }
                        """.formatted(manufacturerId))
                .exchange()
                .expectStatus().isOk();

        var row = db.findById(id);
        assertSoftly(softly -> {
            softly.assertThat(row.get("name")).isEqualTo("Zipfer Urquell Premium");
            softly.assertThat(row.get("type")).isEqualTo("LAGER");
        });
    }

    @Test
    void delete_existingId_returnsNoContentAndRemoves() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        var id = db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);

        restTestClient.delete().uri("/api/beers/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        assertSoftly(softly -> softly.assertThat(db.count()).isZero());
    }

    @Test
    void get_unknownId_returnsNotFoundWithProblemDetail() {
        var result = restTestClient.get().uri("/api/beers/{id}", UUID.randomUUID())
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
    void post_invalidBeerType_returnsUnprocessableEntityWithProblemDetail() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");

        var result = restTestClient.post().uri("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Zipfer Urquell",
                          "type": "UNKNOWN_TYPE",
                          "abv": 4.8,
                          "manufacturerId": "%s"
                        }
                        """.formatted(manufacturerId))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(422);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
            softly.assertThat(db.count()).isZero();
        });
    }

    @Test
    void post_unknownManufacturer_returnsNotFoundWithProblemDetail() {
        var result = restTestClient.post().uri("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Zipfer Urquell",
                          "type": "LAGER",
                          "abv": 4.8,
                          "manufacturerId": "%s"
                        }
                        """.formatted(UUID.randomUUID()))
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
            softly.assertThat(db.count()).isZero();
        });
    }

    @Test
    void getAll_returnsPage() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        db.insert("Heineken Pilsner", "PILSNER", 5.0, "A classic pilsner", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?page=0&size=10")
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
