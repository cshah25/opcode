package com.example.opcodeapp.db;

import com.example.opcodeapp.model.Applicant;

import java.util.List;

public interface FirestoreCallbackApplicantsReceive {

    void onDataReceived(List<Applicant> applicant);
    void onError(Exception e);
}
