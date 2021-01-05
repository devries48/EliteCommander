package com.devries48.elitecommander;

import android.app.Application;
import android.content.res.Resources;

public class App extends Application {
    private static App mInstance;
    private static Resources res;

    public static App getInstance() {
        return mInstance;
    }

    // Usage: App.getRes().getString(R.string.some_id)
    public static Resources getRes() {
        return res;
    }

    public static String getResString(Integer resId) {
        return res.getString(resId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        res = getResources();
    }
}
