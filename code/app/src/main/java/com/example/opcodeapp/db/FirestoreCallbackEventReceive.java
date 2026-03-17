package com.example.opcodeapp.db;

import com.example.opcodeapp.model.Event;

import com.example.opcodeapp.model.Event;

public interface FirestoreCallbackEventReceive {
    void onDataReceived(Event e);
    void onError(Exception e);
}
