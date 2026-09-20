package com.sky.takeout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OrderSubmitDTO {

    @Positive(message = "addressBookId must be greater than 0")
    private Long addressBookId;

    @NotNull(message = "payMethod must not be null")
    @Min(value = 1, message = "payMethod must be 1 or 2")
    @Max(value = 2, message = "payMethod must be 1 or 2")
    private Integer payMethod;

    @Size(max = 255, message = "remark must not exceed 255 characters")
    private String remark;

    public Long getAddressBookId() {
        return addressBookId;
    }

    public void setAddressBookId(Long addressBookId) {
        this.addressBookId = addressBookId;
    }

    public Integer getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(Integer payMethod) {
        this.payMethod = payMethod;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
