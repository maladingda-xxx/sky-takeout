package com.sky.takeout;

import com.fasterxml.jackson.databind.JsonNode;
import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.OrderService;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.OrderSubmitVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminOrderHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private DishService dishService;

    @Test
    void shouldManageOrderOverAdminHttp() {
        OrderSubmitVO order = createPaidOrder();

        ResponseEntity<JsonNode> searchResponse = restTemplate.getForEntity(
                "/admin/order/conditionSearch?number="
                        + order.getOrderNumber()
                        + "&withDetails=true&page=1&pageSize=10",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        assertEquals(1, searchResponse.getBody().path("data").path("total").asInt());
        assertEquals(
                1,
                searchResponse.getBody()
                        .path("data")
                        .path("records")
                        .get(0)
                        .path("orderDetailList")
                        .size()
        );

        ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(
                "/admin/order/details/" + order.getId(),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertEquals(2, detailResponse.getBody().path("data").path("status").asInt());

        ResponseEntity<JsonNode> confirmResponse = restTemplate.exchange(
                "/admin/order/confirm",
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("id", order.getId())),
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());

        ResponseEntity<JsonNode> deliveryResponse = restTemplate.exchange(
                "/admin/order/delivery/" + order.getId(),
                HttpMethod.PUT,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, deliveryResponse.getStatusCode());

        ResponseEntity<JsonNode> completeResponse = restTemplate.exchange(
                "/admin/order/complete/" + order.getId(),
                HttpMethod.PUT,
                null,
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, completeResponse.getStatusCode());

        ResponseEntity<JsonNode> completedDetail = restTemplate.getForEntity(
                "/admin/order/details/" + order.getId(),
                JsonNode.class
        );
        assertEquals(5, completedDetail.getBody().path("data").path("status").asInt());

        ResponseEntity<JsonNode> statisticsResponse = restTemplate.getForEntity(
                "/admin/order/statistics",
                JsonNode.class
        );
        assertEquals(HttpStatus.OK, statisticsResponse.getStatusCode());
        assertNotNull(statisticsResponse.getBody());
        assertNotNull(statisticsResponse.getBody().path("data").path("toBeConfirmed"));
    }

    private OrderSubmitVO createPaidOrder() {
        UserContext.setUserId(99L);
        try {
            Long dishId = createDish();
            createAddress();

            ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
            itemDTO.setDishId(dishId);
            itemDTO.setQuantity(1);
            shoppingCartService.add(itemDTO);

            OrderSubmitDTO submitDTO = new OrderSubmitDTO();
            submitDTO.setPayMethod(1);
            OrderSubmitVO submitted = orderService.submit(submitDTO);

            OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
            paymentDTO.setOrderNumber(submitted.getOrderNumber());
            orderService.pay(paymentDTO);
            return submitted;
        } finally {
            UserContext.clear();
        }
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("Admin HTTP Order Dish");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("20.00"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private void createAddress() {
        AddressBookCreateDTO dto = new AddressBookCreateDTO();
        dto.setConsignee("Admin HTTP User");
        dto.setSex(1);
        dto.setPhone("13800000011");
        dto.setProvinceName("Shanghai");
        dto.setCityName("Shanghai");
        dto.setDistrictName("Pudong");
        dto.setDetail("Admin HTTP Road");
        dto.setIsDefault(1);
        addressBookService.create(dto);
    }
}
