package com.example.opcodeapp;
import android.content.Context;
import android.provider.Settings;
/**
 * Utility class for getting a device identifier
 */

public final class DeviceIdUtil {
    private DeviceIdUtil(){}

    /**
     * Returns the Android device id
     *
     * @param context application or activity context
     * @return device id string
     */
    public static String getDeviceId(Context context){
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
  }
}