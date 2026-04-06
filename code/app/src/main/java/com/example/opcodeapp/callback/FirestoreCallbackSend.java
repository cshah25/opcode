package com.example.opcodeapp.callback;

/**
 * Callback for results you don't care about
 */
public interface FirestoreCallbackSend {
    void onSendSuccess(Void unused);
    void onSendFailure(Exception e);
}
