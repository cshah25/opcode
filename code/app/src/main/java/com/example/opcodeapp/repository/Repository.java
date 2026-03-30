package com.example.opcodeapp.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Abstract class representing a repository for specific model classes
 */
public abstract class Repository {
    protected final FirebaseFirestore db;
    protected final CollectionReference ref;

    /**
     * Constructor for the Repository class
     *
     * @param db    The firebase firestore instance
     * @param refId The id of the collection the repository is handling
     */
    public Repository(FirebaseFirestore db, String refId) {
        this.db = db;
        this.ref = db.collection(refId);
    }
}
