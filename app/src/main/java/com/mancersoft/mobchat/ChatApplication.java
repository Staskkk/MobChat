package com.mancersoft.mobchat;

import android.app.Application;

import com.sendbird.android.SendBird;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init("4CBF41B6-011B-4682-B9ED-B67A97F39426", this);
    }
}
