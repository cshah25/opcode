package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Applicant;

import java.util.List;

public interface FirestoreCallbackApplicantsReceive {

    void onDataReceived(List<Applicant> applicant);
    void onError(Exception e);
}
