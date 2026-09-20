package com.sky.takeout.vo;

import java.time.LocalDateTime;

public class EmployeeVO {

    private final Long id;
    private final String name;
    private final String username;
    private final String phone;
    private final Integer sex;
    private final Integer status;
    private final LocalDateTime updateTime;

    public EmployeeVO(
            Long id,
            String name,
            String username,
            String phone,
            Integer sex,
            Integer status,
            LocalDateTime updateTime
    ) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.sex = sex;
        this.status = status;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getSex() {
        return sex;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
