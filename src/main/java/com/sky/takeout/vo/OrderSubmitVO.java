package com.sky.takeout.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSubmitVO {

    private final Long id;
    private final String orderNumber;
    private final BigDecimal amount;
    private final LocalDateTime orderTime;

    public OrderSubmitVO(
            Long id,
            String orderNumber,
            BigDecimal amount,
            LocalDateTime orderTime
    ) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.orderTime = orderTime;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }
}
