package com.sky.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OrderRejectionDTO {

    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;

    @NotBlank(message = "reason must not be blank")
    @Size(max = 255, message = "reason must not exceed 255 characters")
    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
