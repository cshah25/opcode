package com.example.opcodeapp.Testing;

import static java.security.AccessController.getContext;

import android.widget.Toast;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackSend;
import com.example.opcodeapp.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;

public class FirebaseTest {

    private DBManager dbManager = new DBManager(FirebaseFirestore.getInstance());


    Event.Builder b = Event.builder("")
            .name("Test Event 1")
            .location("Test Location 1")
            .description("Test Description 1")
            .start(LocalDateTime.of(2026, 8, 1, 12, 0))
            .end(LocalDateTime.of(2026, 8, 2, 12, 0))
            .registrationStart(LocalDateTime.now())
            .registrationEnd(LocalDateTime.of(2026, 3, 24, 12, 0));

    Event event = b.build();

    dbManager.addEvent(event, new FirestoreCallbackSend() {

        @Override
        public void onSendSuccess(Void unused) {
            Toast.makeText(getContext(), "Event created", Toast.LENGTH_SHORT).show();

        }



    }


}




