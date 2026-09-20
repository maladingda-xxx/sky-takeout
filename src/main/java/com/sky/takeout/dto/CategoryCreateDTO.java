package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryCreateDTO {

    @NotNull(message = "type must not be null")
    @Min(value = 1, message = "type must be 1 or 2")
    @Max(value = 2, message = "type must be 1 or 2")
    private Integer type;

    @NotBlank(message = "name must not be blank")
    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    @NotNull(message = "sort must not be null")
    @Min(value = 0, message = "sort must be greater than or equal to 0")
    private Integer sort = 0;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
