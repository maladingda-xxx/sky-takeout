package com.sky.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserLoginDTO {

    @NotBlank(message = "code must not be blank")
    @Size(max = 128, message = "code must not exceed 128 characters")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
