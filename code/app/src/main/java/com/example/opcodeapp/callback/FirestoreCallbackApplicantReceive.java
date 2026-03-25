package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Applicant;

public interface FirestoreCallbackApplicantReceive {

    void onDataReceived(Applicant applicant);

    void onError(Exception e);
}
