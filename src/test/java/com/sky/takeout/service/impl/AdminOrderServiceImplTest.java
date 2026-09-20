package com.sky.takeout.service.impl;

import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.OrderAdminCancelDTO;
import com.sky.takeout.dto.OrderConfirmDTO;
import com.sky.takeout.dto.OrderRejectionDTO;
import com.sky.takeout.entity.Order;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.OrderMapper;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    @Test
    void shouldNormalizeAdminPageQuery() {
        AdminOrderPageQueryDTO query = new AdminOrderPageQueryDTO();
        query.setPage(0);
        query.setPageSize(500);
        query.setNumber("  NO123  ");
        query.setPhone("  138  ");

        OrderVO order = orderVO(1L, 2);
        when(orderMapper.countByAdminQuery(query)).thenReturn(1L);
        when(orderMapper.selectAdminPage(query)).thenReturn(List.of(order));

        PageResult<OrderVO> result = adminOrderService.pageQuery(query);

        ArgumentCaptor<AdminOrderPageQueryDTO> captor =
                ArgumentCaptor.forClass(AdminOrderPageQueryDTO.class);
        verify(orderMapper).countByAdminQuery(captor.capture());

        assertEquals(1, result.getTotal());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(100, captor.getValue().getPageSize());
        assertEquals("NO123", captor.getValue().getNumber());
        assertEquals("138", captor.getValue().getPhone());
    }

    @Test
    void shouldReturnEmptyAdminPageWithoutSelectingRows() {
        AdminOrderPageQueryDTO query = new AdminOrderPageQueryDTO();
        when(orderMapper.countByAdminQuery(query)).thenReturn(0L);

        PageResult<OrderVO> result = adminOrderService.pageQuery(query);

        assertEquals(0, result.getTotal());
        verify(orderMapper, never()).selectAdminPage(any());
    }

    @Test
    void shouldRejectInvalidAdminTimeRange() {
        AdminOrderPageQueryDTO query = new AdminOrderPageQueryDTO();
        query.setBeginTime(LocalDateTime.of(2026, 9, 21, 0, 0));
        query.setEndTime(LocalDateTime.of(2026, 9, 20, 0, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminOrderService.pageQuery(query)
        );

        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldConfirmOrder() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 2));
        when(orderMapper.transitionStatus(1L, 2, 3)).thenReturn(1);

        OrderConfirmDTO confirmDTO = new OrderConfirmDTO();
        confirmDTO.setId(1L);
        adminOrderService.confirm(confirmDTO);

        verify(orderMapper).transitionStatus(1L, 2, 3);
    }

    @Test
    void shouldRejectOrderWithNormalizedReason() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 2));
        when(orderMapper.reject(1L, "Out of stock")).thenReturn(1);

        OrderRejectionDTO rejectionDTO = new OrderRejectionDTO();
        rejectionDTO.setId(1L);
        rejectionDTO.setReason("  Out of stock  ");
        adminOrderService.reject(rejectionDTO);

        verify(orderMapper).reject(1L, "Out of stock");
    }

    @Test
    void shouldCancelDeliveringOrder() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 4));
        when(orderMapper.adminCancel(1L, "Customer request")).thenReturn(1);

        OrderAdminCancelDTO cancelDTO = new OrderAdminCancelDTO();
        cancelDTO.setId(1L);
        cancelDTO.setReason("Customer request");
        adminOrderService.cancel(cancelDTO);

        verify(orderMapper).adminCancel(1L, "Customer request");
    }

    @Test
    void shouldAdvanceOrderToDeliveryAndCompletion() {
        when(orderMapper.selectById(1L))
                .thenReturn(order(1L, 3), order(1L, 4));
        when(orderMapper.transitionStatus(1L, 3, 4)).thenReturn(1);
        when(orderMapper.transitionStatus(1L, 4, 5)).thenReturn(1);

        adminOrderService.delivery(1L);
        adminOrderService.complete(1L);

        verify(orderMapper).transitionStatus(1L, 3, 4);
        verify(orderMapper).transitionStatus(1L, 4, 5);
    }

    @Test
    void shouldRejectTransitionForWrongStatus() {
        when(orderMapper.selectById(1L)).thenReturn(order(1L, 5));

        OrderConfirmDTO confirmDTO = new OrderConfirmDTO();
        confirmDTO.setId(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminOrderService.confirm(confirmDTO)
        );

        assertEquals(409, exception.getCode());
        verify(orderMapper, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void shouldReturnOrderStatistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO(2, 3, 4);
        when(orderMapper.selectStatistics()).thenReturn(statistics);

        OrderStatisticsVO result = adminOrderService.statistics();

        assertEquals(2, result.getToBeConfirmed());
        assertEquals(3, result.getConfirmed());
        assertEquals(4, result.getDeliveryInProgress());
    }

    @Test
    void shouldReturnNotFoundForMissingOrder() {
        when(orderMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminOrderService.getById(99L)
        );

        assertEquals(404, exception.getCode());
        assertEquals("Order not found", exception.getMessage());
    }

    private Order order(Long id, int status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setPayStatus(1);
        return order;
    }

    private OrderVO orderVO(Long id, int status) {
        OrderVO order = new OrderVO();
        order.setId(id);
        order.setStatus(status);
        return order;
    }
}
