package com.sky.takeout.vo;

import java.util.List;

public class DishFlavorVO {

    private final String name;
    private final List<String> value;

    public DishFlavorVO(String name, List<String> value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public List<String> getValue() {
        return value;
    }
}
