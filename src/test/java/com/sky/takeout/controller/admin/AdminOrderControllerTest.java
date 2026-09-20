package com.sky.takeout.controller.admin;

import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.service.AdminOrderService;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOrderService adminOrderService;

    @Test
    void shouldSearchOrdersAndReturnDetails() throws Exception {
        OrderVO order = new OrderVO();
        order.setId(1L);
        order.setNumber("NO123");
        order.setStatus(2);
        order.setAmount(new BigDecimal("56.00"));

        when(adminOrderService.pageQuery(any()))
                .thenReturn(new PageResult<>(1, List.of(order)));
        when(adminOrderService.getById(1L)).thenReturn(order);

        mockMvc.perform(get("/admin/order/conditionSearch")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("number", "NO")
                        .param("beginTime", "2026-09-20 00:00:00")
                        .param("endTime", "2026-09-20 23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].number").value("NO123"));

        mockMvc.perform(get("/admin/order/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldReturnOrderStatistics() throws Exception {
        when(adminOrderService.statistics())
                .thenReturn(new OrderStatisticsVO(2, 3, 4));

        mockMvc.perform(get("/admin/order/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.toBeConfirmed").value(2))
                .andExpect(jsonPath("$.data.confirmed").value(3))
                .andExpect(jsonPath("$.data.deliveryInProgress").value(4));
    }

    @Test
    void shouldConfirmRejectCancelDeliverAndCompleteOrder() throws Exception {
        doNothing().when(adminOrderService).confirm(any());
        doNothing().when(adminOrderService).reject(any());
        doNothing().when(adminOrderService).cancel(any());
        doNothing().when(adminOrderService).delivery(any());
        doNothing().when(adminOrderService).complete(any());

        mockMvc.perform(put("/admin/order/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/order/rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1, "reason": "Out of stock"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/order/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1, "reason": "Customer request"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/order/delivery/1"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/admin/order/complete/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectRejectionWithoutReason() throws Exception {
        mockMvc.perform(put("/admin/order/rejection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 1, "reason": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
