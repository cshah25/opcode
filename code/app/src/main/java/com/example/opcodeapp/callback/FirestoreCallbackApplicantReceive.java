package com.example.opcodeapp.callback;

import androidx.annotation.Nullable;

import com.example.opcodeapp.model.Applicant;

/**
 * Callback to receive an applicant
 */
public interface FirestoreCallbackApplicantReceive {

    void onDataReceived(@Nullable Applicant applicant);

    void onError(Exception e);
}
