package com.sky.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "sky.auth.enabled=true"
)
@ActiveProfiles("test")
class UserCatalogHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldBrowseCatalogWithoutLogin() {
        ResponseEntity<JsonNode> categories = restTemplate.getForEntity(
                "/user/category/list?type=1",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, categories.getStatusCode());
        assertNotNull(categories.getBody());
        assertTrue(categories.getBody().path("data").isArray());

        ResponseEntity<JsonNode> dishes = restTemplate.getForEntity(
                "/user/dish/list?categoryId=1",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, dishes.getStatusCode());

        ResponseEntity<JsonNode> profile = restTemplate.getForEntity(
                "/user/profile",
                JsonNode.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, profile.getStatusCode());
    }
}
