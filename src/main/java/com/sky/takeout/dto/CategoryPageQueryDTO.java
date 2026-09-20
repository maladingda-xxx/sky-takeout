package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class CategoryPageQueryDTO {

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    @Max(value = 100, message = "pageSize must be less than or equal to 100")
    private int pageSize = 10;

    @Min(value = 1, message = "type must be 1 or 2")
    @Max(value = 2, message = "type must be 1 or 2")
    private Integer type;

    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

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
}
