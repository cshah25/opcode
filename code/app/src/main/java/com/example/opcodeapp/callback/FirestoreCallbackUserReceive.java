package com.example.opcodeapp.callback;

import androidx.annotation.Nullable;

import com.example.opcodeapp.model.User;

public interface FirestoreCallbackUserReceive {
    void onDataReceived(@Nullable User user);
    void onError(Exception e);
}
