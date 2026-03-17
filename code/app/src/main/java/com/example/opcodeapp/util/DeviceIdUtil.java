package com.example.opcodeapp.util;
import android.companion.DeviceId;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

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

    public static DeviceId fromString(String id) {
            return new DeviceId.Builder().setCustomId(id).build();
    }
}