package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.User;

import com.example.opcodeapp.model.User;

import java.util.List;

/**
 * Callback to get a list of users
 */
public interface FirestoreCallbackUsersReceive {

    void onDataReceived(List<User> items);
    void onError(Exception e);
}
