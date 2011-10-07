package com.ideasandroid.e2p;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("E2P_PREFS", 0);
    }
}