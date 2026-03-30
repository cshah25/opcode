package com.example.opcodeapp.callback;

public interface FirestoreCallbackSend {
    void onSendSuccess(Void unused);
    void onSendFailure(Exception e);
}
