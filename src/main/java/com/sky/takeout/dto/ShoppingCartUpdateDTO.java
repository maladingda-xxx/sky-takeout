package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ShoppingCartUpdateDTO {

    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;

    @NotNull(message = "quantity must not be null")
    @Min(value = 1, message = "quantity must be greater than or equal to 1")
    @Max(value = 99, message = "quantity must be less than or equal to 99")
    private Integer quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
