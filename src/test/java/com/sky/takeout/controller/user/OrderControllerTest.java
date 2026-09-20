package com.sky.takeout.controller.user;

import com.sky.takeout.service.OrderService;
import com.sky.takeout.vo.OrderSubmitVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldSubmitOrder() throws Exception {
        when(orderService.submit(any())).thenReturn(new OrderSubmitVO(
                1L,
                "NO123",
                new BigDecimal("56.00"),
                LocalDateTime.of(2026, 9, 20, 18, 0)
        ));

        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payMethod": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").value("NO123"));
    }

    @Test
    void shouldPayAndCancelOrder() throws Exception {
        doNothing().when(orderService).pay(any());
        doNothing().when(orderService).cancel(any(), any());

        mockMvc.perform(put("/user/order/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderNumber": "NO123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/user/order/cancel/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Changed plans"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetOrderHistoryAndDetail() throws Exception {
        OrderVO order = new OrderVO();
        order.setId(1L);
        order.setNumber("NO123");
        order.setStatus(1);
        order.setAmount(new BigDecimal("56.00"));

        when(orderService.pageQuery(any()))
                .thenReturn(new PageResult<>(1, List.of(order)));
        when(orderService.getById(1L)).thenReturn(order);

        mockMvc.perform(get("/user/order/history")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].number").value("NO123"));

        mockMvc.perform(get("/user/order/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
