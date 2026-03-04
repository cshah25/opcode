package com.example.opcodeapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the current logged-in user locally on the device
 */
public final class SessionManager{
    private static final String PREF_NAME = "opcode_prefs";
    private static final String KEY_USER_ID = "user_id";

    private SessionManager(){}

    /**
     * Saves the current user id.
     *
     * @param context context
     * @param userId firestore user id
     */
    public static void saveUserId(Context context, String userId){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Gets the current user id.
     *
     * @param context context
     * @return saved user id, null if none exists
     */
    public static String getUserId(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Clears local session data.
     *
     * @param context context
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();
    }
}