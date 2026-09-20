package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class OrderPageQueryDTO {

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    @Max(value = 100, message = "pageSize must be less than or equal to 100")
    private int pageSize = 10;

    @Min(value = 1, message = "status must be between 1 and 6")
    @Max(value = 6, message = "status must be between 1 and 6")
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
