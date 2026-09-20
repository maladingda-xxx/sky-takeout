package com.sky.takeout.mapper;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.AddressBookCreateDTO;
import com.sky.takeout.dto.DishCreateDTO;
import com.sky.takeout.dto.OrderCancelDTO;
import com.sky.takeout.dto.OrderPageQueryDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.service.AddressBookService;
import com.sky.takeout.service.DishService;
import com.sky.takeout.service.OrderService;
import com.sky.takeout.service.ShoppingCartService;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private DishService dishService;

    @BeforeEach
    void setUpContext() {
        UserContext.setUserId(77L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldCompleteOrderLifecycle() {
        Long dishId = createDish();
        addressBookService.create(addressDTO());

        ShoppingCartItemDTO itemDTO = new ShoppingCartItemDTO();
        itemDTO.setDishId(dishId);
        itemDTO.setQuantity(2);
        shoppingCartService.add(itemDTO);

        OrderSubmitDTO submitDTO = new OrderSubmitDTO();
        submitDTO.setPayMethod(1);
        OrderSubmitVO submitted = orderService.submit(submitDTO);

        assertEquals(new BigDecimal("36.00"), submitted.getAmount());
        assertTrue(shoppingCartService.list().isEmpty());

        OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
        paymentDTO.setOrderNumber(submitted.getOrderNumber());
        orderService.pay(paymentDTO);

        OrderVO detail = orderService.getById(submitted.getId());
        assertEquals(2, detail.getStatus());
        assertEquals(1, detail.getPayStatus());
        assertEquals(1, detail.getOrderDetailList().size());

        PageResult<OrderVO> history = orderService.pageQuery(
                new OrderPageQueryDTO()
        );
        assertEquals(1, history.getTotal());

        OrderCancelDTO cancelDTO = new OrderCancelDTO();
        cancelDTO.setReason("Changed plans");
        orderService.cancel(submitted.getId(), cancelDTO);
        assertEquals(6, orderService.getById(submitted.getId()).getStatus());
    }

    private Long createDish() {
        DishCreateDTO dto = new DishCreateDTO();
        dto.setName("Order Dish");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("18.00"));
        dto.setStatus(1);
        return dishService.create(dto);
    }

    private AddressBookCreateDTO addressDTO() {
        AddressBookCreateDTO dto = new AddressBookCreateDTO();
        dto.setConsignee("Order User");
        dto.setSex(1);
        dto.setPhone("13800000009");
        dto.setProvinceName("Shanghai");
        dto.setCityName("Shanghai");
        dto.setDistrictName("Pudong");
        dto.setDetail("Order Road");
        return dto;
    }
}
