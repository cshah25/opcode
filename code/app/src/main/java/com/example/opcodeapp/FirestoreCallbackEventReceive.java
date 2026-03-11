package com.example.opcodeapp;

public interface FirestoreCallbackEventReceive {
    void onDataReceived(Event e);
    void onError(Exception e);
}
