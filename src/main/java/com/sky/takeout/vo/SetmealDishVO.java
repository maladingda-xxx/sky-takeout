package com.sky.takeout.vo;

import java.math.BigDecimal;

public class SetmealDishVO {

    private final Long dishId;
    private final String dishName;
    private final BigDecimal dishPrice;
    private final Integer copies;

    public SetmealDishVO(
            Long dishId,
            String dishName,
            BigDecimal dishPrice,
            Integer copies
    ) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.dishPrice = dishPrice;
        this.copies = copies;
    }

    public Long getDishId() {
        return dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public BigDecimal getDishPrice() {
        return dishPrice;
    }

    public Integer getCopies() {
        return copies;
    }
}
