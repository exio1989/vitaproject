package com.test.exio.testapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

/**
 * Created by exio1989 on 29.10.2015.
 */
public class Credentials {
    public static final String BASIC_AUTHORITY = "BASIC_AUTHORITY";
    public static String getBasicAuthority(Context context)
    {
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences sharedPreferences = context.getSharedPreferences(BASIC_AUTHORITY, mode);
        return sharedPreferences.getString("credentials",null);
    }

    public static void setBasicAuthority(Context context, String username,String password)
    {
        String credentials = username + ":" + password;
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences.Editor editor = context.getSharedPreferences(BASIC_AUTHORITY, mode).edit();
        editor.putString("credentials", credentials);
        editor.apply();
    }

    public static void clearBasicAuthority(Context context) {
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences.Editor editor = context.getSharedPreferences(BASIC_AUTHORITY, mode).edit();
        editor.putString("credentials", null);
        editor.apply();
    }
}
