package com.sky.takeout.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DishDetailVO {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal price;
    private final String image;
    private final String description;
    private final Integer status;
    private final LocalDateTime updateTime;
    private final List<DishFlavorVO> flavors;

    public DishDetailVO(
            Long id,
            String name,
            Long categoryId,
            String categoryName,
            BigDecimal price,
            String image,
            String description,
            Integer status,
            LocalDateTime updateTime,
            List<DishFlavorVO> flavors
    ) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.image = image;
        this.description = description;
        this.status = status;
        this.updateTime = updateTime;
        this.flavors = flavors;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public List<DishFlavorVO> getFlavors() {
        return flavors;
    }
}
