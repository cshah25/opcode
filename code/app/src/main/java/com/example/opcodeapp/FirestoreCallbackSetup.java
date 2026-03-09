package com.example.opcodeapp;

import android.content.Context;
import android.widget.Toast;

public class FirestoreCallbackSetup implements FirestoreCallbackSend {
    Context ctx;

    public FirestoreCallbackSetup(Context context) {
        ctx = context;
    }
    @Override
    public void onSendSuccess() {
        Toast.makeText(ctx, "Account successfully created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendFailure(Exception e) {
        Toast.makeText(ctx, String.format("Error creating account: %s", e.toString()), Toast.LENGTH_SHORT).show();
    }
}
