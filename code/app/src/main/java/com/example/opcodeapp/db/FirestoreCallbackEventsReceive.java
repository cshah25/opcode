package com.example.opcodeapp.db;

import com.example.opcodeapp.model.Event;

import com.example.opcodeapp.model.Event;

import java.util.List;

/**
 * Interface for receiving users from DB.
 */
public interface FirestoreCallbackEventsReceive {
    void onDataReceived(List<Event> items);
    void onError(Exception e);
}
