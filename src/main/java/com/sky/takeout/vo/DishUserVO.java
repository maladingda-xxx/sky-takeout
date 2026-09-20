package com.sky.takeout.vo;

import java.math.BigDecimal;
import java.util.List;

public class DishUserVO {

    private final Long id;
    private final String name;
    private final BigDecimal price;
    private final String image;
    private final String description;
    private final List<DishFlavorVO> flavors;

    public DishUserVO(
            Long id,
            String name,
            BigDecimal price,
            String image,
            String description,
            List<DishFlavorVO> flavors
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.flavors = flavors;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public List<DishFlavorVO> getFlavors() {
        return flavors;
    }
}
