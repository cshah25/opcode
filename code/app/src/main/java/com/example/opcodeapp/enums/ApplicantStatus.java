package com.example.opcodeapp.enums;

import com.example.opcodeapp.model.Event;

/**
 * Represents the status of applicants to an {@link Event}
 */
public enum ApplicantStatus {
    NOT_DRAWN,
    INVITED,
    ACCEPTED,
    DECLINED,
    DECLINED_REMOVED;

    public String displayName() {
        String name = name();
        return name.charAt(0) + name.toLowerCase().substring(1);
    }
}
