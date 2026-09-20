package com.sky.takeout.vo;

import java.time.LocalDateTime;

public class CategoryVO {

    private final Long id;
    private final Integer type;
    private final String name;
    private final Integer sort;
    private final Integer status;
    private final LocalDateTime updateTime;

    public CategoryVO(
            Long id,
            Integer type,
            String name,
            Integer sort,
            Integer status,
            LocalDateTime updateTime
    ) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.sort = sort;
        this.status = status;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Integer getSort() {
        return sort;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
