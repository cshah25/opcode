package com.example.opcodeapp;

import java.util.List;

public interface FirestoreCallbackUsersReceive {

    void onDataReceived(List<User> items);
    void onError(Exception e);
}
