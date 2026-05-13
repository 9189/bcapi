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
    void getAll_searchByName_returnsMatchingBeers() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        var zipfer1Id = db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        var zipfer2Id = db.insert("Zipfer Dark", "DUNKEL", 5.2, "A dark lager", manufacturerId);
        db.insert("Amstel", "LAGER", 5.0, "A Dutch lager", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?search=ZIPFER")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        var ids = items.stream().map(item -> (String) item.get("id")).toList();
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(2);
            softly.assertThat(ids).containsExactlyInAnyOrder(zipfer1Id.toString(), zipfer2Id.toString());
        });
    }

    @Test
    void getAll_searchByType_returnsMatchingBeers() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        var ipaId = db.insert("Punk IPA", "IPA", 5.6, "A hoppy IPA", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?search=ipa")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(1);
            softly.assertThat(items.getFirst().get("id")).isEqualTo(ipaId.toString());
        });
    }

    @Test
    void getAll_searchByManufacturerName_returnsMatchingBeers() {
        var heinekenId = manufacturerDb.insert("Heineken", "NL");
        var estrellaId = manufacturerDb.insert("Estrella Galicia", "ES");
        db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", heinekenId);
        var mahouId = db.insert("Mahou Classic", "LAGER", 5.0, "A Spanish lager", estrellaId);

        var result = restTestClient.get().uri("/api/beers?search=galicia")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(1);
            softly.assertThat(items.getFirst().get("id")).isEqualTo(mahouId.toString());
        });
    }

    @Test
    void getAll_searchByAbv_returnsMatchingBeers() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        var zipferId = db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        db.insert("Amstel", "LAGER", 5.0, "A Dutch lager", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?search=4.8")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(1);
            softly.assertThat(items.getFirst().get("id")).isEqualTo(zipferId.toString());
        });
    }

    @Test
    void getAll_invalidPageSize_returnsBadRequestWithProblemDetail() {
        var result = restTestClient.get().uri("/api/beers?size=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(400);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
        });
    }

    @Test
    void getAll_sortedByNameAscending_returnsItemsInOrder() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        db.insert("Amstel", "LAGER", 5.0, "A Dutch lager", manufacturerId);
        db.insert("Moretti", "LAGER", 4.6, "An Italian lager", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?page=0&size=10&sortBy=name&sortDirection=asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(3);
            softly.assertThat(items.get(0).get("name")).isEqualTo("Amstel");
            softly.assertThat(items.get(1).get("name")).isEqualTo("Moretti");
            softly.assertThat(items.get(2).get("name")).isEqualTo("Zipfer Urquell");
        });
    }

    @Test
    void getAll_sortedByAbvDescending_returnsItemsInOrder() {
        var manufacturerId = manufacturerDb.insert("Heineken", "NL");
        db.insert("Zipfer Urquell", "LAGER", 4.8, "A classic lager", manufacturerId);
        db.insert("Amstel", "LAGER", 5.0, "A Dutch lager", manufacturerId);
        db.insert("Moretti", "LAGER", 4.6, "An Italian lager", manufacturerId);

        var result = restTestClient.get().uri("/api/beers?page=0&size=10&sortBy=abv&sortDirection=desc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult();

        var items = (List<Map<String, Object>>) Objects.requireNonNull(result.getResponseBody()).get("items");
        assertSoftly(softly -> {
            softly.assertThat(items).hasSize(3);
            softly.assertThat(items.get(0).get("name")).isEqualTo("Amstel");
            softly.assertThat(items.get(1).get("name")).isEqualTo("Zipfer Urquell");
            softly.assertThat(items.get(2).get("name")).isEqualTo("Moretti");
        });
    }

    @Test
    void getAll_invalidSortBy_returnsBadRequestWithProblemDetail() {
        var result = restTestClient.get().uri("/api/beers?sortBy=invalid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(400);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
        });
    }

    @Test
    void getAll_invalidSortDirection_returnsBadRequestWithProblemDetail() {
        var result = restTestClient.get().uri("/api/beers?sortDirection=invalid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(400);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
        });
    }

    @Test
    void post_missingRequiredField_returnsBadRequestWithProblemDetail() {
        var result = restTestClient.post().uri("/api/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "type": "LAGER",
                          "abv": 4.8
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult();

        var body = Objects.requireNonNull(result.getResponseBody());
        assertSoftly(softly -> {
            softly.assertThat(body.get("status")).isEqualTo(400);
            softly.assertThat(body.get("title")).isNotNull();
            softly.assertThat(body.get("detail")).isNotNull();
            softly.assertThat(db.count()).isZero();
        });
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

        var result = restTestClient.get().uri("/api/beers?page=0&size=10&sortBy=name&sortDirection=asc")
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
