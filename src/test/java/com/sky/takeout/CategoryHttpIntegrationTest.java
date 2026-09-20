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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteCategoryHttpLifecycle() {
        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                "/admin/category",
                Map.of(
                        "type", 1,
                        "name", "HTTP Category",
                        "sort", 15
                ),
                JsonNode.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        long id = createResponse.getBody().path("data").asLong();

        ResponseEntity<JsonNode> listResponse = restTemplate.getForEntity(
                "/admin/category/list?type=1",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertEquals(2, listResponse.getBody().path("data").size());

        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(
                "/admin/category",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "id", id,
                        "type", 1,
                        "name", "Updated HTTP Category",
                        "sort", 5
                )),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<JsonNode> statusResponse = restTemplate.postForEntity(
                "/admin/category/status/0?id=" + id,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        ResponseEntity<JsonNode> deleteResponse = restTemplate.exchange(
                "/admin/category/" + id,
                HttpMethod.DELETE,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        ResponseEntity<JsonNode> missingResponse = restTemplate.getForEntity(
                "/admin/category/" + id,
                JsonNode.class
        );
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
        assertNotNull(missingResponse.getBody());
        assertEquals(404, missingResponse.getBody().path("code").asInt());
    }
}
