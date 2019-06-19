package com.arcsoft.arcfacedemo.faceserver;

import com.arcsoft.arcfacedemo.db.dao.RegisterInfo;

public class CompareRgResult {

    private float similar;
    private RegisterInfo mRegisterInfo;

    public CompareRgResult(RegisterInfo mRegisterInfo, float similar) {
        this.mRegisterInfo = mRegisterInfo;
        this.similar = similar;
    }

    public float getSimilar() {
        return similar;
    }

    public void setSimilar(float similar) {
        this.similar = similar;
    }

    public RegisterInfo getRegisterInfo() {
        return mRegisterInfo;
    }

    public void setRegisterInfo(RegisterInfo registerInfo) {
        mRegisterInfo = registerInfo;
    }
}
