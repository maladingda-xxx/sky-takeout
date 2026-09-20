package com.sky.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DishHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteDishHttpLifecycle() {
        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                "/admin/dish",
                Map.of(
                        "name", "HTTP Dish",
                        "categoryId", 1,
                        "price", 19.90,
                        "image", "/images/http-dish.jpg",
                        "description", "HTTP test dish",
                        "status", 0,
                        "flavors", List.of(Map.of(
                                "name", "Spiciness",
                                "value", List.of("Spicy", "Mild")
                        ))
                ),
                JsonNode.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        long id = createResponse.getBody().path("data").asLong();

        ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(
                "/admin/dish/" + id,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertNotNull(detailResponse.getBody());
        assertEquals(
                "Hot Dishes",
                detailResponse.getBody().path("data").path("categoryName").asText()
        );
        assertEquals(
                "Spicy",
                detailResponse.getBody().path("data").path("flavors").get(0)
                        .path("value").get(0).asText()
        );

        ResponseEntity<JsonNode> pageResponse = restTemplate.getForEntity(
                "/admin/dish/page?page=1&pageSize=10&name=HTTP",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());
        assertNotNull(pageResponse.getBody());
        assertEquals(1, pageResponse.getBody().path("data").path("total").asInt());

        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(
                "/admin/dish",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "id", id,
                        "name", "Updated HTTP Dish",
                        "categoryId", 1,
                        "price", 21.90,
                        "description", "Updated",
                        "flavors", List.of(Map.of(
                                "name", "Spiciness",
                                "value", List.of("Mild")
                        ))
                )),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<JsonNode> statusResponse = restTemplate.postForEntity(
                "/admin/dish/status/1?id=" + id,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        ResponseEntity<JsonNode> deleteResponse = restTemplate.exchange(
                "/admin/dish/" + id,
                HttpMethod.DELETE,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        ResponseEntity<JsonNode> missingResponse = restTemplate.getForEntity(
                "/admin/dish/" + id,
                JsonNode.class
        );
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
    }
}
