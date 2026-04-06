package com.example.opcodeapp.util;

import android.os.Parcel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Util for dates
 */
public class DateUtil {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * @param dt date to be converted
     * @return a date converted to the epoch second
     */
    public static long toSeconds(LocalDateTime dt) {
        return dt.atZone(ZoneId.systemDefault())
                .toInstant()
                .getEpochSecond();
    }

    public static LocalDateTime fromSeconds(long l) {
        return Instant.ofEpochSecond(l)
                .atZone(ZoneOffset.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime fromMillis(long l) {
        return Instant.ofEpochMilli(l)
                .atZone(ZoneOffset.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime fromParcel(Parcel in) {
        Class<LocalDateTime> classs = LocalDateTime.class;
        return in.readSerializable(classs.getClassLoader(), classs);
    }

    public static String toString(DateTimeFormatter formatter, LocalDateTime dateTime) {
        return formatter.format(dateTime);
    }

    public static String toString(LocalDateTime dateTime) {
        return toString(FORMAT, dateTime);
    }
}

