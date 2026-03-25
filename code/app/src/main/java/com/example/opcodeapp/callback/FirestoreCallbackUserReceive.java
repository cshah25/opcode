package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.User;

public interface FirestoreCallbackUserReceive {
    void onDataReceived(User u);
    void onError(Exception e);
}
