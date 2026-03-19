package com.example.opcodeapp.db;

public interface FirestoreCallbackSend {
    void onSendSuccess(Void unused);
    void onSendFailure(Exception e);
}
