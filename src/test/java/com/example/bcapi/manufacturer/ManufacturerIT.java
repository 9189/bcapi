package com.example.bcapi.manufacturer;

import com.example.bcapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureRestTestClient
class ManufacturerIT {

    @Autowired
    RestTestClient restTestClient;

    @Test
    void post_returnsCreated() {
        restTestClient.post().uri("/api/manufacturers")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "string",
                          "originCountry": "string"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Zipfer Urquell");
    }
}
