package com.example.opcodeapp;

import java.util.List;

public interface FirestoreCallbackUserReceive {
    void onDataReceived(User u);
    void onError(Exception e);
}
