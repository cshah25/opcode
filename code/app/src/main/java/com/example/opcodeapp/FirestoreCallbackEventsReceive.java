package com.example.opcodeapp;

import java.util.List;

public interface FirestoreCallbackEventsReceive {
    void onDataReceived(List<Event> items);
    void onError(Exception e);
}
