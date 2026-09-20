package com.sky.takeout.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SetmealDetailVO {

    private final Long id;
    private final Long categoryId;
    private final String categoryName;
    private final String name;
    private final BigDecimal price;
    private final Integer status;
    private final String description;
    private final String image;
    private final LocalDateTime updateTime;
    private final List<SetmealDishVO> dishes;

    public SetmealDetailVO(
            Long id,
            Long categoryId,
            String categoryName,
            String name,
            BigDecimal price,
            Integer status,
            String description,
            String image,
            LocalDateTime updateTime,
            List<SetmealDishVO> dishes
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.price = price;
        this.status = status;
        this.description = description;
        this.image = image;
        this.updateTime = updateTime;
        this.dishes = dishes;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public List<SetmealDishVO> getDishes() {
        return dishes;
    }
}
