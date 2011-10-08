package com.ideasandroid.e2p;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {
    public C2DMReceiver() {
        super(Config.SENDER_Id);
    }

    @Override
    public void onRegistered(Context context, String registration) {
        DeviceRegistrar.registerWithServer(context, registration);
    }

    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        String deviceRegistrationID = prefs.getString("deviceRegistrationID", null);
        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {
        context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d("***********：Received Message:", "hahahaha************:"+extras.getString("message"));
        if (extras != null) {
            String msgType = (String) extras.get("msgType");
            String message = (String) extras.get("message");

            if (msgType != null && message != null) {
                SharedPreferences settings = Prefs.get(context);
                Intent launchIntent = LauncherUtils.getLaunchIntent(context, msgType, message);
                // Notify and optionally start activity
                if (settings.getBoolean("launchBrowserOrMaps", true) && launchIntent != null) {
                    try {
                        context.startActivity(launchIntent);
                        LauncherUtils.playNotificationSound(context);
                    } catch (ActivityNotFoundException e) {
                        return;
                    }
                } else {
                    if ("1".equals(msgType)) {  // have selection
                        LauncherUtils.generateNotification(context, message,
                                context.getString(R.string.copied_desktop_clipboard), launchIntent);
                    } else {
                        LauncherUtils.generateNotification(context, message, "E2P消息", launchIntent);
                    }
                }
            }
        }
        
    }
}
