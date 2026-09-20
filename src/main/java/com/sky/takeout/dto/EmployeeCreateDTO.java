package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmployeeCreateDTO {

    @NotBlank(message = "name must not be blank")
    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    @NotBlank(message = "username must not be blank")
    @Size(max = 32, message = "username must not exceed 32 characters")
    private String username;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 32, message = "password length must be between 6 and 32")
    private String password;

    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^1\\d{10}$", message = "phone must be a valid 11-digit number")
    private String phone;

    @NotNull(message = "sex must not be null")
    @Min(value = 1, message = "sex must be 1 or 2")
    @Max(value = 2, message = "sex must be 1 or 2")
    private Integer sex;

    @NotBlank(message = "idNumber must not be blank")
    @Pattern(
            regexp = "^\\d{17}[0-9Xx]$",
            message = "idNumber must be a valid 18-character ID number"
    )
    private String idNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
