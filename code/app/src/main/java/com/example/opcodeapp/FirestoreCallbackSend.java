package com.example.opcodeapp;

public interface FirestoreCallbackSend {
    void onSendSuccess();
    void onSendFailure(Exception e);
}
