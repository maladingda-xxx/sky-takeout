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
class SetmealHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteSetmealHttpLifecycle() {
        long dishId = createDish();
        long setmealId = createSetmeal(dishId);

        ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(
                "/admin/setmeal/" + setmealId,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertNotNull(detailResponse.getBody());
        assertEquals(
                "Set Meals",
                detailResponse.getBody().path("data").path("categoryName").asText()
        );
        assertEquals(
                "HTTP Setmeal Dish",
                detailResponse.getBody().path("data").path("dishes").get(0)
                        .path("dishName").asText()
        );

        ResponseEntity<JsonNode> pageResponse = restTemplate.getForEntity(
                "/admin/setmeal/page?page=1&pageSize=10&name=HTTP",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, pageResponse.getStatusCode());
        assertNotNull(pageResponse.getBody());
        assertEquals(1, pageResponse.getBody().path("data").path("total").asInt());

        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(
                "/admin/setmeal",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "id", setmealId,
                        "categoryId", 2,
                        "name", "Updated HTTP Setmeal",
                        "price", 75.00,
                        "description", "Updated setmeal",
                        "dishes", List.of(Map.of(
                                "dishId", dishId,
                                "copies", 2
                        ))
                )),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<JsonNode> statusResponse = restTemplate.postForEntity(
                "/admin/setmeal/status/1?id=" + setmealId,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, statusResponse.getStatusCode());

        ResponseEntity<JsonNode> deleteSetmealResponse = restTemplate.exchange(
                "/admin/setmeal/" + setmealId,
                HttpMethod.DELETE,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteSetmealResponse.getStatusCode());

        ResponseEntity<JsonNode> deleteDishResponse = restTemplate.exchange(
                "/admin/dish/" + dishId,
                HttpMethod.DELETE,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deleteDishResponse.getStatusCode());
    }

    private long createDish() {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/admin/dish",
                Map.of(
                        "name", "HTTP Setmeal Dish",
                        "categoryId", 1,
                        "price", 22.00,
                        "status", 0,
                        "flavors", List.of()
                ),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().path("data").asLong();
    }

    private long createSetmeal(long dishId) {
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/admin/setmeal",
                Map.of(
                        "categoryId", 2,
                        "name", "HTTP Family Meal",
                        "price", 68.00,
                        "status", 0,
                        "dishes", List.of(Map.of(
                                "dishId", dishId,
                                "copies", 1
                        ))
                ),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().path("data").asLong();
    }
}
