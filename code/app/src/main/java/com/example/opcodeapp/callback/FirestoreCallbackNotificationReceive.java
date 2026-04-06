package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Notification;

/**
 * Callback to receive a notification
 */
public interface FirestoreCallbackNotificationReceive {
    void onDataReceived(Notification item);
    void onError(Exception e);
}
