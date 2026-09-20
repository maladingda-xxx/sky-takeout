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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "sky.auth.enabled=true",
                "sky.wechat.mock-enabled=true"
        }
)
@ActiveProfiles("test")
class AddressBookHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldManageAddressesForAuthenticatedUser() {
        String token = loginUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<JsonNode> createResponse = restTemplate.postForEntity(
                "/user/addressBook",
                new HttpEntity<>(addressRequest("Home", 0), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        long addressId = createResponse.getBody().path("data").path("id").asLong();
        assertEquals(1, createResponse.getBody().path("data").path("isDefault").asInt());

        ResponseEntity<JsonNode> defaultResponse = restTemplate.exchange(
                "/user/addressBook/default",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, defaultResponse.getStatusCode());
        assertEquals(
                addressId,
                defaultResponse.getBody().path("data").path("id").asLong()
        );

        ResponseEntity<JsonNode> deleteResponse = restTemplate.exchange(
                "/user/addressBook/" + addressId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    private String loginUser() {
        ResponseEntity<JsonNode> loginResponse = restTemplate.postForEntity(
                "/user/login",
                Map.of("code", "address-test-user"),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        return loginResponse.getBody().path("data").path("token").asText();
    }

    private Map<String, Object> addressRequest(String label, int isDefault) {
        return Map.of(
                "consignee", "Test User",
                "sex", 1,
                "phone", "13800000009",
                "provinceName", "Shanghai",
                "cityName", "Shanghai",
                "districtName", "Pudong",
                "detail", label + " Road",
                "label", label,
                "isDefault", isDefault
        );
    }
}
