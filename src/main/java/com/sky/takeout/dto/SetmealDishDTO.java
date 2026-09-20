package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SetmealDishDTO {

    @NotNull(message = "dishId must not be null")
    @Positive(message = "dishId must be greater than 0")
    private Long dishId;

    @NotNull(message = "copies must not be null")
    @Min(value = 1, message = "copies must be greater than or equal to 1")
    @Max(value = 99, message = "copies must be less than or equal to 99")
    private Integer copies;

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }
}
