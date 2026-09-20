package com.sky.takeout.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderConfirmDTO {

    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
