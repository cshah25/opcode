package com.example.opcodeapp;

import android.content.Context;
import android.widget.Toast;

public class FirestoreCallbackInvitationAccept implements FirestoreCallbackSend {
    Context ctx;

    public FirestoreCallbackInvitationAccept(Context c) {
        ctx = c;
    }

    @Override
    public void onSendSuccess() {
        Toast.makeText(ctx, "Invitation accepted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendFailure(Exception e) {
        Toast.makeText(ctx, String.format("Error accepting invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
    }
}
