package com.sky.takeout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DishUpdateDTO {

    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;

    @NotBlank(message = "name must not be blank")
    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    @NotNull(message = "categoryId must not be null")
    @Positive(message = "categoryId must be greater than 0")
    private Long categoryId;

    @NotNull(message = "price must not be null")
    @DecimalMin(value = "0.01", message = "price must be greater than or equal to 0.01")
    @Digits(integer = 8, fraction = 2, message = "price must have at most 8 integer digits and 2 decimal digits")
    private BigDecimal price;

    @Size(max = 255, message = "image must not exceed 255 characters")
    private String image;

    @Size(max = 255, message = "description must not exceed 255 characters")
    private String description;

    @Valid
    @Size(max = 20, message = "flavors must not contain more than 20 items")
    private List<DishFlavorDTO> flavors = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DishFlavorDTO> getFlavors() {
        return flavors;
    }

    public void setFlavors(List<DishFlavorDTO> flavors) {
        this.flavors = flavors;
    }
}
