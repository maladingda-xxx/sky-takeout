package com.sky.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class DishFlavorDTO {

    @NotBlank(message = "flavor name must not be blank")
    @Size(max = 32, message = "flavor name must not exceed 32 characters")
    private String name;

    @NotEmpty(message = "flavor value must not be empty")
    @Size(max = 20, message = "flavor value must not contain more than 20 items")
    private List<
            @NotBlank(message = "flavor value item must not be blank")
            @Size(max = 32, message = "flavor value item must not exceed 32 characters")
            String> value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
