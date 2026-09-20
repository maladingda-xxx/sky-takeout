package com.sky.takeout.dto;

import jakarta.validation.constraints.Size;

public class OrderCancelDTO {

    @Size(max = 255, message = "reason must not exceed 255 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
