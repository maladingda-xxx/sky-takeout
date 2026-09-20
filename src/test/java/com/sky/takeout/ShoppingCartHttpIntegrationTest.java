package com.sky.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.service.DishService;
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

import java.math.BigDecimal;
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
class ShoppingCartHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DishService dishService;

    @Test
    void shouldCompleteAuthenticatedCartLifecycle() {
        Long dishId = createDish();
        String token = loginUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<JsonNode> addResponse = restTemplate.postForEntity(
                "/user/shoppingCart/add",
                new HttpEntity<>(Map.of(
                        "dishId", dishId,
                        "quantity", 2
                ), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());

        ResponseEntity<JsonNode> listResponse = restTemplate.exchange(
                "/user/shoppingCart/list",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertEquals(1, listResponse.getBody().path("data").size());
        assertEquals(2, listResponse.getBody().path("data").get(0).path("quantity").asInt());

        ResponseEntity<JsonNode> cleanResponse = restTemplate.exchange(
                "/user/shoppingCart/clean",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, cleanResponse.getStatusCode());

        dishService.delete(dishId);
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("HTTP Cart Dish");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("18.00"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private String loginUser() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/user/login",
                Map.of("code", "cart-test-user"),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().path("data").path("token").asText();
    }
}
