package com.joaosimonassi.pocv3sync;

import android.app.Application;
import android.content.Context;
import com.ido.ble.BLEManager;

public class APP extends Application {
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        BLEManager.onApplicationCreate(this);
    }

    public static Context getAppContext(){
        return application.getApplicationContext();
    }
}
