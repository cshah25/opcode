package com.example.opcodeapp.db;

import com.example.opcodeapp.model.User;

public interface FirestoreCallbackUserReceive {
    void onDataReceived(User u);
    void onError(Exception e);
}
