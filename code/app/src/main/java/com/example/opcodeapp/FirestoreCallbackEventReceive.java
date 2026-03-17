package com.example.opcodeapp;

import com.example.opcodeapp.model.Event;

public interface FirestoreCallbackEventReceive {
    void onDataReceived(Event e);
    void onError(Exception e);
}
