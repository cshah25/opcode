package com.example.opcodeapp.callback;

import androidx.annotation.Nullable;

import com.example.opcodeapp.model.Event;

/**
 * Callback to receive an event
 */
public interface FirestoreCallbackEventReceive {
    void onDataReceived(@Nullable Event e);
    void onError(Exception e);
}
