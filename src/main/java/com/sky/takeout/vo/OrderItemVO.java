package com.sky.takeout.vo;

import java.math.BigDecimal;

public class OrderItemVO {

    private final String name;
    private final String image;
    private final Long dishId;
    private final Long setmealId;
    private final String dishFlavor;
    private final Integer number;
    private final BigDecimal amount;

    public OrderItemVO(
            String name,
            String image,
            Long dishId,
            Long setmealId,
            String dishFlavor,
            Integer number,
            BigDecimal amount
    ) {
        this.name = name;
        this.image = image;
        this.dishId = dishId;
        this.setmealId = setmealId;
        this.dishFlavor = dishFlavor;
        this.number = number;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Long getDishId() {
        return dishId;
    }

    public Long getSetmealId() {
        return setmealId;
    }

    public String getDishFlavor() {
        return dishFlavor;
    }

    public Integer getNumber() {
        return number;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
