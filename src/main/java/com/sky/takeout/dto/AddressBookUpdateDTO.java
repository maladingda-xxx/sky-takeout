package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AddressBookUpdateDTO {

    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;

    @NotBlank(message = "consignee must not be blank")
    @Size(max = 32, message = "consignee must not exceed 32 characters")
    private String consignee;

    @NotNull(message = "sex must not be null")
    @Min(value = 1, message = "sex must be 1 or 2")
    @Max(value = 2, message = "sex must be 1 or 2")
    private Integer sex;

    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^1\\d{10}$", message = "phone must be a valid 11-digit number")
    private String phone;

    @NotBlank(message = "provinceName must not be blank")
    @Size(max = 32, message = "provinceName must not exceed 32 characters")
    private String provinceName;

    @NotBlank(message = "cityName must not be blank")
    @Size(max = 32, message = "cityName must not exceed 32 characters")
    private String cityName;

    @NotBlank(message = "districtName must not be blank")
    @Size(max = 32, message = "districtName must not exceed 32 characters")
    private String districtName;

    @NotBlank(message = "detail must not be blank")
    @Size(max = 255, message = "detail must not exceed 255 characters")
    private String detail;

    @Size(max = 32, message = "label must not exceed 32 characters")
    private String label;

    @Min(value = 0, message = "isDefault must be 0 or 1")
    @Max(value = 1, message = "isDefault must be 0 or 1")
    private Integer isDefault = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }
}
