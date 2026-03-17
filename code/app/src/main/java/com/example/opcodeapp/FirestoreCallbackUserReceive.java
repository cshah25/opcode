package com.example.opcodeapp;

import com.example.opcodeapp.model.User;

public interface FirestoreCallbackUserReceive {
    void onDataReceived(User u);
    void onError(Exception e);
}
