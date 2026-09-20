package com.sky.takeout.vo;

public class AddressBookVO {

    private final Long id;
    private final String consignee;
    private final Integer sex;
    private final String phone;
    private final String provinceName;
    private final String cityName;
    private final String districtName;
    private final String detail;
    private final String label;
    private final Integer isDefault;

    public AddressBookVO(
            Long id,
            String consignee,
            Integer sex,
            String phone,
            String provinceName,
            String cityName,
            String districtName,
            String detail,
            String label,
            Integer isDefault
    ) {
        this.id = id;
        this.consignee = consignee;
        this.sex = sex;
        this.phone = phone;
        this.provinceName = provinceName;
        this.cityName = cityName;
        this.districtName = districtName;
        this.detail = detail;
        this.label = label;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public String getConsignee() {
        return consignee;
    }

    public Integer getSex() {
        return sex;
    }

    public String getPhone() {
        return phone;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public String getDetail() {
        return detail;
    }

    public String getLabel() {
        return label;
    }

    public Integer getIsDefault() {
        return isDefault;
    }
}
