package com.sky.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrderPaymentDTO {

    @NotBlank(message = "orderNumber must not be blank")
    @Size(max = 32, message = "orderNumber must not exceed 32 characters")
    private String orderNumber;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
