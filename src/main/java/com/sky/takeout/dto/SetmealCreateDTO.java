package com.sky.takeout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SetmealCreateDTO {

    @NotNull(message = "categoryId must not be null")
    @Positive(message = "categoryId must be greater than 0")
    private Long categoryId;

    @NotBlank(message = "name must not be blank")
    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    @NotNull(message = "price must not be null")
    @DecimalMin(value = "0.01", message = "price must be greater than or equal to 0.01")
    @Digits(integer = 8, fraction = 2, message = "price must have at most 8 integer digits and 2 decimal digits")
    private BigDecimal price;

    @NotNull(message = "status must not be null")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status = 0;

    @Size(max = 255, message = "description must not exceed 255 characters")
    private String description;

    @Size(max = 255, message = "image must not exceed 255 characters")
    private String image;

    @Valid
    @NotEmpty(message = "setmeal must contain at least one dish")
    @Size(max = 50, message = "setmeal must not contain more than 50 dishes")
    private List<SetmealDishDTO> dishes = new ArrayList<>();

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<SetmealDishDTO> getDishes() {
        return dishes;
    }

    public void setDishes(List<SetmealDishDTO> dishes) {
        this.dishes = dishes;
    }
}
