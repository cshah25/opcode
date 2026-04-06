package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Notification;

public interface FirestoreCallbackNotificationReceive {
    void onDataReceived(Notification item);
    void onError(Exception e);
}
