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
class OrderHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DishService dishService;

    @Test
    void shouldSubmitPayAndQueryOrderOverHttp() {
        Long dishId = createDish();
        String token = loginUser();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        createAddress(headers);
        addCart(headers, dishId);

        ResponseEntity<JsonNode> submitResponse = restTemplate.postForEntity(
                "/user/order/submit",
                new HttpEntity<>(Map.of("payMethod", 1), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, submitResponse.getStatusCode());
        assertNotNull(submitResponse.getBody());
        long orderId = submitResponse.getBody().path("data").path("id").asLong();
        String orderNumber = submitResponse.getBody().path("data").path("orderNumber").asText();
        assertFalse(orderNumber.isBlank());

        ResponseEntity<JsonNode> paymentResponse = restTemplate.exchange(
                "/user/order/payment",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("orderNumber", orderNumber), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, paymentResponse.getStatusCode());

        ResponseEntity<JsonNode> historyResponse = restTemplate.exchange(
                "/user/order/history?page=1&pageSize=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertEquals(1, historyResponse.getBody().path("data").path("total").asInt());

        ResponseEntity<JsonNode> detailResponse = restTemplate.exchange(
                "/user/order/detail/" + orderId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertEquals(2, detailResponse.getBody().path("data").path("status").asInt());

        dishService.delete(dishId);
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("Order HTTP Dish");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("18.00"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private String loginUser() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/user/login",
                Map.of("code", "order-test-user"),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().path("data").path("token").asText();
    }

    private void createAddress(HttpHeaders headers) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/user/addressBook",
                new HttpEntity<>(Map.of(
                        "consignee", "Order User",
                        "sex", 1,
                        "phone", "13800000009",
                        "provinceName", "Shanghai",
                        "cityName", "Shanghai",
                        "districtName", "Pudong",
                        "detail", "Order Road",
                        "label", "Home",
                        "isDefault", 1
                ), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void addCart(HttpHeaders headers, Long dishId) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/user/shoppingCart/add",
                new HttpEntity<>(Map.of(
                        "dishId", dishId,
                        "quantity", 1
                ), headers),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
