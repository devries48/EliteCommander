package com.devries48.elitecommander;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static App mInstance;

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}