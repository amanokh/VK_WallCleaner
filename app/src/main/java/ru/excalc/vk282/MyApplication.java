package ru.excalc.vk282;

import android.app.Application;

import com.vk.sdk.VKSdk;

public class MyApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(MyApplication.this);
    }
}
