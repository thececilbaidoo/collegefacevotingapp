package com.biometricsx.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginSharedPref {
    private static final String SPREF_FILE = "SPREF_LOGIN";
    private static final String UID_LOGIN_KEY = "uid";
    private static final String PID_KEY = "PID_KEY";
    private static final String NAME_KEY = "name";
    private static final String PUNCH_LOGIN_KEY = "punch";


    public static String getUIdKey(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sprefLogin.getString(UID_LOGIN_KEY, "");
    }

    public static void setUIdKey(Context context, String uid) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(UID_LOGIN_KEY, uid);
        editor.apply();
    }

    public static void setUnameKey(Context context, String name) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(NAME_KEY, name);
        editor.apply();
    }

    public static String getUnameKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(NAME_KEY, "");
    }

    public static String getPidKey(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sprefLogin.getString(PID_KEY, "");
    }

    public static void setPidKey(Context context, String facerect) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(PID_KEY, facerect);
        editor.apply();
    }

    public static void setPunchInKey(Context context, boolean isClicked) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putBoolean(PUNCH_LOGIN_KEY, isClicked);
        editor.apply();
    }

    public static boolean getPunchInKey(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sprefLogin.getBoolean(PUNCH_LOGIN_KEY, false);
    }
}
