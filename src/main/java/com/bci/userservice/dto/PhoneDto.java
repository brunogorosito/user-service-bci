package com.bci.userservice.dto;

public class PhoneDto {
    private Long number;
    private Integer citycode;
    private String contrycode;

    public PhoneDto() {}

    public PhoneDto(Long number, Integer citycode, String contrycode) {
        this.number = number;
        this.citycode = citycode;
        this.contrycode = contrycode;
    }

    // Getters y Setters
    public Long getNumber() { return number; }
    public void setNumber(Long number) { this.number = number; }

    public Integer getCitycode() { return citycode; }
    public void setCitycode(Integer citycode) { this.citycode = citycode; }

    public String getContrycode() { return contrycode; }
    public void setContrycode(String contrycode) { this.contrycode = contrycode; }
}