package com.example.opcodeapp;

import com.example.opcodeapp.model.Event;

/**
 * Represents the status of applicants to an {@link Event}
 */
public enum ApplicantStatus {
    NOT_DRAWN,
    INVITED,
    ACCEPTED,
    DECLINED,
    DECLINED_REMOVED
}
