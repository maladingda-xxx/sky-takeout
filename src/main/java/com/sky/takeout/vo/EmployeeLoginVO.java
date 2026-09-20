package com.sky.takeout.vo;

public class EmployeeLoginVO {

    private final Long id;
    private final String username;
    private final String name;
    private final String token;

    public EmployeeLoginVO(Long id, String username, String name, String token) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }
}
