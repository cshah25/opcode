package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Applicant;

import java.util.List;

/**
 * Callback to receive a list of applicants
 */
public interface FirestoreCallbackApplicantsReceive {

    void onDataReceived(List<Applicant> applicant);
    void onError(Exception e);
}
