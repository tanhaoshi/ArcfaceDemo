package com.arcsoft.arcfacedemo;

import android.app.Application;
import android.content.Context;

import com.arcsoft.arcfacedemo.db.DaoMaster;
import com.arcsoft.arcfacedemo.db.DaoSession;

import org.greenrobot.greendao.database.Database;

public class FaceApplication extends Application{

    private final static String dbName = "face_db";

    public static FaceApplication sApp;

    private DaoSession daoSession;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        initGreenDao();
    }

    public static FaceApplication getInstance(){
        return sApp;
    }

    private void initGreenDao(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, dbName);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

}
