package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Event;

import com.example.opcodeapp.model.Event;

import java.util.List;

/**
 * Interface for receiving events from DB.
 */
public interface FirestoreCallbackEventsReceive {
    void onDataReceived(List<Event> items);
    void onError(Exception e);
}
