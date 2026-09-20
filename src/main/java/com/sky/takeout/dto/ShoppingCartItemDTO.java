package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ShoppingCartItemDTO {

    @Positive(message = "dishId must be greater than 0")
    private Long dishId;

    @Positive(message = "setmealId must be greater than 0")
    private Long setmealId;

    @Min(value = 1, message = "quantity must be greater than or equal to 1")
    @Max(value = 99, message = "quantity must be less than or equal to 99")
    private Integer quantity = 1;

    @Size(max = 255, message = "flavor must not exceed 255 characters")
    private String flavor;

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Long getSetmealId() {
        return setmealId;
    }

    public void setSetmealId(Long setmealId) {
        this.setmealId = setmealId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }
}
