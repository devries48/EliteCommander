package com.devries48.elitecommander;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application {
    private static App mInstance;
    private static Resources res;

    public static App getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}