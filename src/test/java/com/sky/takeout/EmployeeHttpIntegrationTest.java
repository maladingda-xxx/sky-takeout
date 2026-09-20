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
class EmployeeHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteEmployeeHttpLifecycle() {
        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                "/admin/employee",
                Map.of(
                        "name", "HTTP Smoke User",
                        "username", "http-smoke-user",
                        "password", "secret123",
                        "phone", "13800000008",
                        "sex", 1,
                        "idNumber", "110101199007070077"
                ),
                JsonNode.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals(200, createResponse.getBody().path("code").asInt());
        long id = createResponse.getBody().path("data").asLong();

        ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(
                "/admin/employee/" + id,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertNotNull(detailResponse.getBody());
        assertEquals(
                "http-smoke-user",
                detailResponse.getBody().path("data").path("username").asText()
        );

        Map<String, Object> updateRequest = Map.of(
                "id", id,
                "name", "Updated HTTP Smoke User",
                "username", "http-smoke-user",
                "phone", "13800000008",
                "sex", 2,
                "idNumber", "110101199007070077"
        );
        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(
                "/admin/employee",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<JsonNode> statusResponse = restTemplate.postForEntity(
                "/admin/employee/status/0?id=" + id,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        ResponseEntity<JsonNode> deleteResponse = restTemplate.exchange(
                "/admin/employee/" + id,
                HttpMethod.DELETE,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        ResponseEntity<JsonNode> missingResponse = restTemplate.getForEntity(
                "/admin/employee/" + id,
                JsonNode.class
        );
        assertEquals(HttpStatus.NOT_FOUND, missingResponse.getStatusCode());
        assertNotNull(missingResponse.getBody());
        assertEquals(404, missingResponse.getBody().path("code").asInt());
    }
}
