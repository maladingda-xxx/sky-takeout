package com.sky.takeout.vo;

import java.time.LocalDateTime;

public class EmployeeDetailVO {

    private final Long id;
    private final String name;
    private final String username;
    private final String phone;
    private final Integer sex;
    private final String idNumber;
    private final Integer status;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    public EmployeeDetailVO(
            Long id,
            String name,
            String username,
            String phone,
            Integer sex,
            String idNumber,
            Integer status,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.sex = sex;
        this.idNumber = idNumber;
        this.status = status;
        this.createTime = createTime;
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

    public String getIdNumber() {
        return idNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
