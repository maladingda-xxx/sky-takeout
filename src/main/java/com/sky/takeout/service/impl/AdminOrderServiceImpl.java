package com.sky.takeout.service.impl;

import com.sky.takeout.dto.AdminOrderPageQueryDTO;
import com.sky.takeout.dto.OrderAdminCancelDTO;
import com.sky.takeout.dto.OrderConfirmDTO;
import com.sky.takeout.dto.OrderRejectionDTO;
import com.sky.takeout.entity.Order;
import com.sky.takeout.entity.OrderDetail;
import com.sky.takeout.exception.BusinessException;
import com.sky.takeout.mapper.OrderMapper;
import com.sky.takeout.service.AdminOrderService;
import com.sky.takeout.vo.OrderItemVO;
import com.sky.takeout.vo.OrderStatisticsVO;
import com.sky.takeout.vo.OrderVO;
import com.sky.takeout.vo.PageResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final int PENDING_PAYMENT = 1;
    private static final int TO_BE_ACCEPTED = 2;
    private static final int ACCEPTED = 3;
    private static final int DELIVERING = 4;
    private static final int COMPLETED = 5;
    private static final int MAX_PAGE_SIZE = 100;

    private final OrderMapper orderMapper;

    public AdminOrderServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public PageResult<OrderVO> pageQuery(AdminOrderPageQueryDTO query) {
        normalizeQuery(query);
        validateTimeRange(query);

        long total = orderMapper.countByAdminQuery(query);
        if (total == 0) {
            return new PageResult<>(0, List.of());
        }

        List<OrderVO> records = orderMapper.selectAdminPage(query);
        if (query.isWithDetails()) {
            fillDetails(records);
        }
        return new PageResult<>(total, records);
    }

    @Override
    public OrderVO getById(Long id) {
        Order order = requireOrder(id);
        OrderVO orderVO = toVO(order);
        orderVO.setOrderDetailList(toItemVOs(
                orderMapper.selectDetailsByOrderIds(List.of(id))
        ));
        return orderVO;
    }

    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.selectStatistics();
    }

    @Override
    @Transactional
    public void confirm(OrderConfirmDTO confirmDTO) {
        transition(
                confirmDTO.getId(),
                TO_BE_ACCEPTED,
                ACCEPTED,
                "Order cannot be confirmed"
        );
    }

    @Override
    @Transactional
    public void reject(OrderRejectionDTO rejectionDTO) {
        Order order = requireOrder(rejectionDTO.getId());
        if (order.getStatus() != TO_BE_ACCEPTED) {
            throw conflict("Order cannot be rejected");
        }

        String reason = requireReason(rejectionDTO.getReason());
        if (orderMapper.reject(order.getId(), reason) != 1) {
            throw conflict("Order status changed, please retry");
        }
    }

    @Override
    @Transactional
    public void cancel(OrderAdminCancelDTO cancelDTO) {
        Order order = requireOrder(cancelDTO.getId());
        if (!isCancellable(order.getStatus())) {
            throw conflict("Order cannot be cancelled");
        }

        if (orderMapper.adminCancel(
                order.getId(),
                normalizeOptionalText(cancelDTO.getReason())
        ) != 1) {
            throw conflict("Order status changed, please retry");
        }
    }

    @Override
    @Transactional
    public void delivery(Long id) {
        transition(id, ACCEPTED, DELIVERING, "Order cannot be delivered");
    }

    @Override
    @Transactional
    public void complete(Long id) {
        transition(id, DELIVERING, COMPLETED, "Order cannot be completed");
    }

    private void transition(
            Long id,
            int expectedStatus,
            int targetStatus,
            String conflictMessage
    ) {
        Order order = requireOrder(id);
        if (order.getStatus() != expectedStatus) {
            throw conflict(conflictMessage);
        }

        if (orderMapper.transitionStatus(
                id,
                expectedStatus,
                targetStatus
        ) != 1) {
            throw conflict("Order status changed, please retry");
        }
    }

    private Order requireOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND.value(),
                    "Order not found"
            );
        }
        return order;
    }

    private boolean isCancellable(Integer status) {
        return status == PENDING_PAYMENT
                || status == TO_BE_ACCEPTED
                || status == ACCEPTED
                || status == DELIVERING;
    }

    private void normalizeQuery(AdminOrderPageQueryDTO query) {
        query.setPage(Math.max(query.getPage(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));
        query.setNumber(normalizeOptionalText(query.getNumber()));
        query.setPhone(normalizeOptionalText(query.getPhone()));
    }

    private void validateTimeRange(AdminOrderPageQueryDTO query) {
        if (query.getBeginTime() != null
                && query.getEndTime() != null
                && query.getBeginTime().isAfter(query.getEndTime())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "beginTime must not be after endTime"
            );
        }
    }

    private String requireReason(String reason) {
        String normalized = normalizeOptionalText(reason);
        if (normalized == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Rejection reason must not be blank"
            );
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private OrderVO toVO(Order order) {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(order.getId());
        orderVO.setNumber(order.getNumber());
        orderVO.setStatus(order.getStatus());
        orderVO.setPayStatus(order.getPayStatus());
        orderVO.setPayMethod(order.getPayMethod());
        orderVO.setAmount(order.getAmount());
        orderVO.setRemark(order.getRemark());
        orderVO.setPhone(order.getPhone());
        orderVO.setAddress(order.getAddress());
        orderVO.setConsignee(order.getConsignee());
        orderVO.setOrderTime(order.getOrderTime());
        orderVO.setCheckoutTime(order.getCheckoutTime());
        orderVO.setCancelReason(order.getCancelReason());
        orderVO.setCancelTime(order.getCancelTime());
        return orderVO;
    }

    private List<OrderItemVO> toItemVOs(List<OrderDetail> details) {
        return details.stream()
                .map(detail -> new OrderItemVO(
                        detail.getName(),
                        detail.getImage(),
                        detail.getDishId(),
                        detail.getSetmealId(),
                        detail.getDishFlavor(),
                        detail.getNumber(),
                        detail.getAmount()
                ))
                .toList();
    }

    private void fillDetails(List<OrderVO> orders) {
        if (orders.isEmpty()) {
            return;
        }

        Map<Long, List<OrderDetail>> detailsByOrderId = orderMapper
                .selectDetailsByOrderIds(
                        orders.stream().map(OrderVO::getId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        orders.forEach(order -> order.setOrderDetailList(
                toItemVOs(detailsByOrderId.getOrDefault(order.getId(), List.of()))
        ));
    }

    private BusinessException conflict(String message) {
        return new BusinessException(HttpStatus.CONFLICT.value(), message);
    }
}
