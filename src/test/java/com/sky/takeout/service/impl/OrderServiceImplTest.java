package com.sky.takeout.service.impl;

import com.sky.takeout.common.UserContext;
import com.sky.takeout.dto.OrderCancelDTO;
import com.sky.takeout.dto.OrderPaymentDTO;
import com.sky.takeout.dto.OrderSubmitDTO;
import com.sky.takeout.entity.AddressBook;
import com.sky.takeout.entity.Dish;
import com.sky.takeout.entity.Order;
import com.sky.takeout.entity.OrderDetail;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.AddressBookMapper;
import com.sky.takeout.mapper.DishMapper;
import com.sky.takeout.mapper.OrderMapper;
import com.sky.takeout.mapper.SetmealMapper;
import com.sky.takeout.mapper.ShoppingCartMapper;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.ShoppingCartVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private AddressBookMapper addressBookMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderMapper,
                shoppingCartMapper,
                addressBookMapper,
                dishMapper,
                setmealMapper
        );
        UserContext.setUserId(7L);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void shouldRejectEmptyCart() {
        when(addressBookMapper.selectDefaultByUserId(7L)).thenReturn(address());
        when(shoppingCartMapper.selectByUserId(7L)).thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.submit(submitDTO())
        );

        assertEquals(400, exception.getCode());
        assertEquals("Shopping cart is empty", exception.getMessage());
    }

    @Test
    void shouldCreateOrderAndClearCart() {
        AddressBook address = address();
        ShoppingCartVO cartItem = cartItem();
        Dish dish = dish();

        when(addressBookMapper.selectDefaultByUserId(7L)).thenReturn(address);
        when(shoppingCartMapper.selectByUserId(7L)).thenReturn(List.of(cartItem));
        when(dishMapper.selectById(10L)).thenReturn(dish);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        });
        when(orderMapper.insertDetails(any())).thenReturn(1);

        OrderSubmitVO result = orderService.submit(submitDTO());

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("56.00"), result.getAmount());
        ArgumentCaptor<List<OrderDetail>> detailCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(orderMapper).insertDetails(detailCaptor.capture());
        assertEquals(2, detailCaptor.getValue().get(0).getNumber());
        verify(shoppingCartMapper).deleteByUserId(7L);
    }

    @Test
    void shouldPayPendingOrder() {
        Order order = order(1);
        when(orderMapper.selectByNumberAndUserId("NO123", 7L)).thenReturn(order);
        when(orderMapper.markPaid(1L, 7L)).thenReturn(1);

        OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
        paymentDTO.setOrderNumber("NO123");
        orderService.pay(paymentDTO);

        verify(orderMapper).markPaid(1L, 7L);
    }

    @Test
    void shouldRejectPaymentWhenConditionalUpdateFails() {
        Order order = order(1);
        when(orderMapper.selectByNumberAndUserId("NO123", 7L)).thenReturn(order);
        when(orderMapper.markPaid(1L, 7L)).thenReturn(0);

        OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
        paymentDTO.setOrderNumber("NO123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(paymentDTO)
        );

        assertEquals(409, exception.getCode());
        assertEquals("Order cannot be paid", exception.getMessage());
    }

    @Test
    void shouldRejectPayingCancelledOrder() {
        Order order = order(6);
        when(orderMapper.selectByNumberAndUserId("NO123", 7L)).thenReturn(order);

        OrderPaymentDTO paymentDTO = new OrderPaymentDTO();
        paymentDTO.setOrderNumber("NO123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.pay(paymentDTO)
        );

        assertEquals(409, exception.getCode());
        verify(orderMapper, never()).markPaid(any(), any());
    }

    @Test
    void shouldCancelPaidOrderAsRefund() {
        Order order = order(2);
        order.setPayStatus(1);
        when(orderMapper.selectByIdAndUserId(1L, 7L)).thenReturn(order);
        when(orderMapper.cancel(1L, 7L, "Changed plans")).thenReturn(1);

        OrderCancelDTO cancelDTO = new OrderCancelDTO();
        cancelDTO.setReason("Changed plans");
        orderService.cancel(1L, cancelDTO);

        verify(orderMapper).cancel(1L, 7L, "Changed plans");
    }

    private OrderSubmitDTO submitDTO() {
        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setPayMethod(1);
        return dto;
    }

    private AddressBook address() {
        AddressBook address = new AddressBook();
        address.setId(5L);
        address.setConsignee("Test User");
        address.setPhone("13800000009");
        address.setProvinceName("Shanghai");
        address.setCityName("Shanghai");
        address.setDistrictName("Pudong");
        address.setDetail("No. 1 Road");
        return address;
    }

    private ShoppingCartVO cartItem() {
        ShoppingCartVO item = new ShoppingCartVO();
        item.setId(1L);
        item.setProductId(10L);
        item.setProductType("dish");
        item.setQuantity(2);
        item.setFlavor("Mild");
        return item;
    }

    private Dish dish() {
        Dish dish = new Dish();
        dish.setId(10L);
        dish.setName("Spicy Chicken");
        dish.setPrice(new BigDecimal("28.00"));
        dish.setStatus(1);
        return dish;
    }

    private Order order(int status) {
        Order order = new Order();
        order.setId(1L);
        order.setNumber("NO123");
        order.setStatus(status);
        order.setPayStatus(0);
        return order;
    }
}
