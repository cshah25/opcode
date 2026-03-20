package com.example.opcodeapp.Testing;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseTest {

    private DBManager dbManager = new DBManager(FirebaseFirestore.getInstance());


    Event testEvent = new Event();



}
