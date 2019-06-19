package com.arcsoft.arcfacedemo.common;


public class MainRc {

    String name;

    String number;

    Integer imgPath;

    public MainRc(String name, String number, Integer imgPath) {
        this.name = name;
        this.number = number;
        this.imgPath = imgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getImgPath() {
        return imgPath;
    }

    public void setImgPath(Integer imgPath) {
        this.imgPath = imgPath;
    }
}
