package com.arcsoft.arcfacedemo.db.manager;

import com.arcsoft.arcfacedemo.FaceApplication;
import com.arcsoft.arcfacedemo.db.RegisterInfoDao;
import com.arcsoft.arcfacedemo.db.dao.RegisterInfo;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class RegisterInfoManager {

   public static RegisterInfoManager sRegisterInfoManager;

   private RegisterInfoManager(){}

   public static RegisterInfoManager getInstance(){
       if(sRegisterInfoManager == null){
           sRegisterInfoManager = new RegisterInfoManager();
       }
       return sRegisterInfoManager;
   }

   public void insertRegisterInfo(RegisterInfo registerInfo){
       RegisterInfoDao dao = FaceApplication.getInstance().getDaoSession().getRegisterInfoDao();
       dao.insert(registerInfo);
   }

   public List<RegisterInfo> getRegisterInfo(){
       RegisterInfoDao dao = FaceApplication.getInstance().getDaoSession().getRegisterInfoDao();
       QueryBuilder queryBuilder = dao.queryBuilder();
       List<RegisterInfo> registerInfos = queryBuilder.list();
       return registerInfos;
   }

   public void clearRegisterFace(){
       RegisterInfoDao dao = FaceApplication.getInstance().getDaoSession().getRegisterInfoDao();
       dao.deleteAll();
   }
}

