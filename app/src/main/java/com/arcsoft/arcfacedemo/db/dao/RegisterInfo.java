package com.arcsoft.arcfacedemo.db.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class RegisterInfo {
    @Id(autoincrement = true)
    private Long id;
    private byte[] featureDate;
    private String name;
    private String age;
    private String sex;
    private String serial;
    private int trackId;
    private String path;
    
    public RegisterInfo(byte[] featureDate,String name ,String age,String sex,String serial,int trackId,String path){
        this.featureDate = featureDate;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.serial = serial;
        this.trackId = trackId;
        this.path = path;
    }

    public RegisterInfo(byte[] featureDate,String name){
        this.featureDate = featureDate;
        this.name = name;
    }

    @Generated(hash = 1470244328)
    public RegisterInfo() {
    }

    @Generated(hash = 863879802)
    public RegisterInfo(Long id, byte[] featureDate, String name, String age, String sex, String serial, int trackId,
            String path) {
        this.id = id;
        this.featureDate = featureDate;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.serial = serial;
        this.trackId = trackId;
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public byte[] getFeatureDate() {
        return featureDate;
    }

    public void setFeatureDate(byte[] featureDate) {
        this.featureDate = featureDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
