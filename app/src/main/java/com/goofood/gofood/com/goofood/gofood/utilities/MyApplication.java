package com.goofood.gofood.com.goofood.gofood.utilities;

import android.app.Application;
import android.content.Context;

/**
 * Created by Thanuj on 5/14/2016.
 */
public class MyApplication extends Application {

    private static MyApplication sInstance;

    @Override

    public void onCreate(){

        super.onCreate();
        sInstance = this;
    }

    public static MyApplication getsInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }
}
