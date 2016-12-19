package com.zonesciences.pyrros.Timer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Peter on 19/12/2016.
 */
public class ButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.print("intent: " + intent.getExtras());
        int notificationId = intent.getIntExtra("notificationId", 0);
        String test = intent.getStringExtra("test");

        System.out.println("onReceive. notification id " + notificationId + " test: " + test);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
