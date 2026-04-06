package com.example.opcodeapp.util;

import android.companion.DeviceId;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

/**
 * Utility methods for working with the device identifier used by the app.
 */
public final class DeviceIdUtil {
    /**
     * Prevents this utility class from being instantiated.
     */
    private DeviceIdUtil() {
    }

    /**
     * Returns the Android secure device identifier for the supplied context.
     *
     * @param context application or activity context
     * @return device id string
     */
    public static String getDeviceId(@NonNull Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /**
     * Wraps a raw string identifier in a {@link DeviceId} instance.
     *
     * @param id the identifier to convert
     * @return a {@link DeviceId} containing the supplied identifier
     */
    @NonNull
    public static DeviceId fromString(@NonNull String id) {
        return new DeviceId.Builder().setCustomId(id).build();
    }
}
