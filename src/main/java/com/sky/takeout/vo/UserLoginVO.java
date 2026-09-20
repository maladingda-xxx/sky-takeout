package com.sky.takeout.vo;

public class UserLoginVO {

    private final Long id;
    private final String openid;
    private final String token;

    public UserLoginVO(Long id, String openid, String token) {
        this.id = id;
        this.openid = openid;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getOpenid() {
        return openid;
    }

    public String getToken() {
        return token;
    }
}
