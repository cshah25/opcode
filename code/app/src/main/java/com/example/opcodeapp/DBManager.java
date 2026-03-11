package com.example.opcodeapp;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.List;


/**
 * The Firestore database manager. manages all the queries needed for the app to run.
 */
public class DBManager {

    /**
     * FirebaseFirestore instance.
     */
    private FirebaseFirestore db;

    /**
     * CollectionReference for the "Users" collection.
     */
    private CollectionReference usersRef;

    /**
     * CollectionReference for the "Events" collection.
     */
    private CollectionReference eventsRef;




    /**
     * Constructor for DBmanager.
     * Initializes the FirebaseFirestore instance.
     */
    public DBManager(FirebaseFirestore DB) {
        this.db = DB;

        usersRef = db.collection("Users");


        eventsRef = db.collection("Events");

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
        // 1. Create a reference to a new document with a generated ID
        // calling .document() without arguments creates a unique ID locally
        DocumentReference newDocRef = db.collection("Events").document();

        // 2. Set the ID inside your event object so it's ready for the Activity
        String generatedId = newDocRef.getId();
        event.setId(generatedId);

        // 3. Use .set() to save the object to that specific reference
        newDocRef.set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Success! The 'event' object now has the correct ID
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
     * Updates an event in the "Events" collection in Firestore.
     *
     * @param event
     * The updated event.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void updateEvent(Event event, FirestoreCallbackSend listener) {
        // 1. Extract the ID directly from the event object
        String docId = event.getId();

        // 2. Safety check: Ensure the ID isn't null or empty before trying to update
        if (docId == null || docId.isEmpty()) {
            listener.onSendFailure(new Exception("Event ID is missing. Cannot update."));
            return;
        }

        // 3. Perform the update using the extracted ID
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
        // 1. Pre-generate the reference to get the ID immediately
        DocumentReference newDocRef = db.collection("Users").document();

        // 2. Set the ID in your User object
        String generatedId = newDocRef.getId();
        user.setId(generatedId);

        // 3. Use .set() to save the user data
        newDocRef.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Success! The 'user' object in your Activity now has its ID
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
     * Updates a user in the "Users" collection in Firestore.
     *
     * @param user
     * The updated user.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void updateUser(User user, FirestoreCallbackSend listener) {
        // 1. Get the ID from the object itself
        String docId = user.getId();

        // 2. Safety check: stop if the ID is missing
        if (docId == null || docId.isEmpty()) {
            listener.onSendFailure(new Exception("User ID is missing. Update aborted."));
            return;
        }

        // 3. Perform the merge update
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

    /**
     * Fetches users with the given device id from the "Users" collection in Firestore
     * and notifies the listener.
     *
     * @param deviceId
     * The device id to search for.
     * @param listener
     * The listener to be notified of the success or failure of the operation.
     */
    public void fetchUserByDeviceId(String deviceId, FirestoreCallbackUsersReceive listener) {
        usersRef
                .whereEqualTo("deviceId", deviceId)
                .get()
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

    public void fetchUserByFirebaseId(String id, FirestoreCallbackUserReceive listener) {
        usersRef.document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User u = task.getResult().toObject(User.class);
                            listener.onDataReceived(u); // Send data back to Activity
                        } else {
                            listener.onError(task.getException());
                        }
                    }
                });
    }

    public void fetchEventByFirebaseId(String id, FirestoreCallbackEventReceive listener) {
        eventsRef.document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Event e = task.getResult().toObject(Event.class);
                            listener.onDataReceived(e); // Send data back to Activity
                        } else {
                            listener.onError(task.getException());
                        }
                    }
                });
    }
}
