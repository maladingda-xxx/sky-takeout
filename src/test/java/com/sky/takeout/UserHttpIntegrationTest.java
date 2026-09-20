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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "sky.auth.enabled=true",
                "sky.wechat.mock-enabled=true"
        }
)
@ActiveProfiles("test")
class UserHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldLoginWithMockWeChatAndAccessProfile() {
        ResponseEntity<JsonNode> loginResponse = restTemplate.postForEntity(
                "/user/login",
                Map.of("code", "integration-demo"),
                JsonNode.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        String token = loginResponse.getBody().path("data").path("token").asText();
        long userId = loginResponse.getBody().path("data").path("id").asLong();
        assertFalse(token.isBlank());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<JsonNode> profileResponse = restTemplate.exchange(
                "/user/profile",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, profileResponse.getStatusCode());
        assertNotNull(profileResponse.getBody());
        assertEquals(userId, profileResponse.getBody().path("data").path("id").asLong());

        ResponseEntity<JsonNode> adminLogin = restTemplate.postForEntity(
                "/admin/employee/login",
                Map.of("username", "admin", "password", "password"),
                JsonNode.class
        );
        String adminToken = adminLogin.getBody().path("data").path("token").asText();

        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(adminToken);
        ResponseEntity<JsonNode> rejected = restTemplate.exchange(
                "/user/profile",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                JsonNode.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, rejected.getStatusCode());
    }
}
