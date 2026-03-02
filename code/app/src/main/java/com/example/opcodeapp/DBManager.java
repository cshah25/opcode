package com.example.opcodeapp;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.List;

public class DBManager {

    /**
     * FirebaseFirestore instance.
     */
    private FirebaseFirestore db;


    /**
     * CollectionReference for the "Users" collection.
     */
    private CollectionReference usersRef = db.collection("Users");


    /**
     * CollectionReference for the "Events" collection.
     */
    private CollectionReference eventsRef = db.collection("Events");

    /**
     * Constructor for DBmanager.
     * Initializes the FirebaseFirestore instance.
     */
    public DBManager() {
        db = FirebaseFirestore.getInstance();
    };




    //Many other methods may be needed for events/users such as deleting, searching for events/users based on attributes.


    /**
     * Adds an event to the "Events" collection in Firestore.
     *
     * @param event
     * The event to be added.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void addEvent(Event event, FirestoreCallbackSend listener) {
        // .add() automatically generates a unique Document ID for you
        db.collection("Events")
                .add(event)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Tell the activity it worked!
                        listener.onSendSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tell the activity what went wrong
                        listener.onSendFailure(e);
                    }
                });
    }



    /**
     * Updates an event in the "Events" collection in Firestore.
     *
     * @param docId
     * The ID of the event to be updated.
     * @param event
     * The updated event.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void updateEvent(String docId, Event event, FirestoreCallbackSend listener) {
        // Set with merge: true will update only the fields present in your object
        db.collection("Events").document(docId)
                .set(event, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSendSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onSendFailure(e);
                    }
                });
    }

    /**
     * Fetches events from the "Events" collection in Firestore and notifies the listener.
     *
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void fetchEvents(FirestoreCallbackEventsReceive listener) {
        db.collection("Events").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Event> items = task.getResult().toObjects(Event.class);
                            listener.onDataReceived(items); // Send data back to Activity
                        } else {
                            listener.onError(task.getException());
                        }
                    }
                });

    }


    /**
     * Adds a user to the "Users" collection in Firestore.
     *
     * @param user
     * The user to be added.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void addUser(User user, FirestoreCallbackSend listener) {
        // .add() automatically generates a unique Document ID for you
        db.collection("Users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Tell the activity it worked!
                        listener.onSendSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Tell the activity what went wrong
                        listener.onSendFailure(e);
                    }
                });
    }


    /**
     * Updates a user in the "Users" collection in Firestore.
     *
     * @param docId
     * The ID of the user to be updated.
     * @param user
     * The updated user.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void updateUser(String docId, User user, FirestoreCallbackSend listener) {
        // Set with merge: true will update only the fields present in your object
        db.collection("Users").document(docId)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSendSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onSendFailure(e);
                    }
                });
    }

    /**
     * Fetches users from the "Users" collection in Firestore and notifies the listener.
     *
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void fetchUsers(FirestoreCallbackUsersReceive listener) {
        db.collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<User> items = task.getResult().toObjects(User.class);
                            listener.onDataReceived(items); // Send data back to Activity
                        } else {
                            listener.onError(task.getException());
                        }
                    }
                });

    }
}
