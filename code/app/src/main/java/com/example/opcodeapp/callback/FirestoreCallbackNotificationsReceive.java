package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Notification;

import java.util.List;

/**
 * Interface for receiving notifications from DB.
 */
public interface FirestoreCallbackNotificationsReceive {
    void onDataReceived(List<Notification> items);
    void onError(Exception e);
}
