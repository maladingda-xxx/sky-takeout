package com.sky.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "sky.auth.enabled=true"
)
@ActiveProfiles("test")
class AuthHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldProtectAdminEndpointsWithJwt() {
        ResponseEntity<JsonNode> unauthorized = restTemplate.getForEntity(
                "/admin/employee/page?page=1&pageSize=10",
                JsonNode.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.getStatusCode());
        assertNotNull(unauthorized.getBody());
        assertEquals(401, unauthorized.getBody().path("code").asInt());

        ResponseEntity<JsonNode> loginResponse = restTemplate.postForEntity(
                "/admin/employee/login",
                Map.of("username", "admin", "password", "password"),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());

        String token = loginResponse.getBody().path("data").path("token").asText();
        long employeeId = loginResponse.getBody().path("data").path("id").asLong();
        assertFalse(token.isBlank());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<JsonNode> authorized = restTemplate.exchange(
                "/admin/employee/page?page=1&pageSize=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, authorized.getStatusCode());
        assertNotNull(authorized.getBody());
        assertEquals(200, authorized.getBody().path("code").asInt());

        HttpHeaders rawTokenHeaders = new HttpHeaders();
        rawTokenHeaders.set("token", token);
        ResponseEntity<JsonNode> rawTokenHeader = restTemplate.exchange(
                "/admin/category/page?page=1&pageSize=10",
                HttpMethod.GET,
                new HttpEntity<>(rawTokenHeaders),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, rawTokenHeader.getStatusCode());

        ResponseEntity<JsonNode> disableSelf = restTemplate.postForEntity(
                "/admin/employee/status/0?id=" + employeeId,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.CONFLICT, disableSelf.getStatusCode());

        ResponseEntity<JsonNode> deleteSelf = restTemplate.exchange(
                "/admin/employee/" + employeeId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.CONFLICT, deleteSelf.getStatusCode());
    }
}
