package com.sky.takeout.vo;

public class UserVO {

    private final Long id;
    private final String name;
    private final String phone;
    private final Integer sex;
    private final String avatar;

    public UserVO(Long id, String name, String phone, Integer sex, String avatar) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getSex() {
        return sex;
    }

    public String getAvatar() {
        return avatar;
    }
}
