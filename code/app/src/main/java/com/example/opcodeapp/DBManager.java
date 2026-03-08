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

import java.util.ArrayList;
import java.util.Arrays;
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
        db.collection("Users")
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

    /**
     * Deletes a user document from the "Users" collection in Firestore.
     *
     * @param user
     * The user to delete.
     * @param listener
     * The listener to be notified of success or failure.
     */
    public void deleteUser(User user, FirestoreCallbackSend listener) {
        db.collection("Users").document(user.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
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
     * Deletes a user's profile if they are not organizing any events.
     * Also removes the user from applicants and attendees of all events.
     *
     * @param user
     * The user whose profile is to be deleted.
     * @param listener
     * The listener to be notified of success or failure.
     */
    public void deleteProfile(User user, FirestoreCallbackSend listener) {

        fetchEvents(new FirestoreCallbackEventsReceive() {
            @Override
            public void onDataReceived(List<Event> events) {
                if (events == null) {
                    listener.onSendFailure(new Exception("Could not fetch events."));
                    return;
                }

                // First check whether the user is organizing any event
                for (Event event : events) {
                    if (event == null || event.getOrganizer() == null) {
                        continue;
                    }

                    String organizerId = event.getOrganizer().getId();
                    if (organizerId != null && organizerId.equals(user.getId())) {
                        listener.onSendFailure(new Exception("Cannot delete profile while organizing an event."));
                        return;
                    }
                }

                // If not organizing, remove the user from applicants and attendees
                for (Event event : events) {
                    if (event == null) {
                        continue;
                    }

                    boolean changed = false;

                    User[] applicants = event.getApplicants();
                    if (applicants != null) {
                        ArrayList<User> updatedApplicants = new ArrayList<>();
                        for (User applicant : applicants) {
                            if (applicant == null) {
                                continue;
                            }

                            if (applicant.getId() != null && applicant.getId().equals(user.getId())) {
                                changed = true;
                            } else {
                                updatedApplicants.add(applicant);
                            }
                        }
                        event.setApplicants(updatedApplicants.toArray(new User[0]));
                    }

                    User[] attendees = event.getAttendees();
                    if (attendees != null) {
                        ArrayList<User> updatedAttendees = new ArrayList<>();
                        for (User attendee : attendees) {
                            if (attendee == null) {
                                continue;
                            }

                            if (attendee.getId() != null && attendee.getId().equals(user.getId())) {
                                changed = true;
                            } else {
                                updatedAttendees.add(attendee);
                            }
                        }
                        event.setAttendees(updatedAttendees.toArray(new User[0]));
                    }

                    if (changed) {
                        updateEvent(event, new FirestoreCallbackSend() {
                            @Override
                            public void onSendSuccess() {
                                // nothing extra needed here
                            }

                            @Override
                            public void onSendFailure(Exception e) {
                                // optional: could report immediately, but keeping minimal for now
                            }
                        });
                    }
                }

                // Finally delete the user document
                deleteUser(user, listener);
            }

            @Override
            public void onError(Exception e) {
                listener.onSendFailure(e);
            }
        });
    }
}
