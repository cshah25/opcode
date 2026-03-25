package com.example.opcodeapp.repository;

import com.google.firebase.firestore.FirebaseFirestore;

public class CommentRepository {
    private final FirebaseFirestore db;

    public CommentRepository(FirebaseFirestore db) {
        this.db = db;
    }


}
