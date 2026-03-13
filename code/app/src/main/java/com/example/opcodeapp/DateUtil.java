package com.example.opcodeapp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DateUtil {

    /**
     * @param dt date to be converted
     * @return a date converted to the epoch second
     */
    public static long toLong(LocalDateTime dt) {
        return dt.atZone(ZoneId.systemDefault())
                .toInstant()
                .getEpochSecond();
    }

    public static LocalDateTime fromLong(long l) {
        return Instant.ofEpochSecond(l)
                .atZone(ZoneOffset.systemDefault())
                .toLocalDateTime();
    }
}
