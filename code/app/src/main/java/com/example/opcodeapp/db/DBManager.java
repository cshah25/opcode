package com.example.opcodeapp.db;

import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


// TODO: Separate into different repository classes
public class DBManager {

    // Constants
    private static final String EVENTS_COLLECTION = "events";
    private static final String USERS_COLLECTION = "users";
    private static final String APPLICANTS_COLLECTION = "applicants";
    private static final String COMMENTS_COLLECTION = "comments";

    private CollectionReference usersRef;
    private CollectionReference eventsRef;
    private CollectionReference applicantsRef;
    private CollectionReference commentsRef;

    private final FirebaseFirestore db;

    /**
     * Constructor for DBManager. Initializes the FirebaseFirestore instance.
     */
    public DBManager(FirebaseFirestore DB) {
        this.db = DB;
        this.usersRef = db.collection(USERS_COLLECTION);
        this.eventsRef = db.collection(EVENTS_COLLECTION);
        this.applicantsRef = db.collection(APPLICANTS_COLLECTION);
        this.commentsRef = db.collection(COMMENTS_COLLECTION);
    }

    //Many other methods may be needed for events/users such as deleting, searching for events/users based on attributes.

    /**
     * Adds an event to the Events collection in Firestore.
     *
     * @param event    The event to be added.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void addEvent(Event event, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = eventsRef.document();
        event.setId(newDocRef.getId());
        newDocRef.set(event.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }


    /**
     * Updates an event in the Events collection in Firestore.
     *
     * @param event    The updated event.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void updateEvent(Event event, FirestoreCallbackSend listener) {
        db.collection(EVENTS_COLLECTION).document(event.getId())
                .set(event, SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Fetches events from the Events collection in Firestore and notifies the listener.
     *
     * @param filter   The filter options to be applied to the query
     * @param listener The listener to be notified of the success or failure of the operation.
     *
     */
    public void fetchEvents(Consumer<Query> filter, FirestoreCallbackEventsReceive listener) {
        CollectionReference ref = db.collection(EVENTS_COLLECTION);
        filter.accept(ref);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Event> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> data = document.getData();
                    Event event = Event.fromMap(document.getId(), data);
                    items.add(event);
                }
                listener.onDataReceived(items);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    public void deleteEvent(Event event, FirestoreCallbackSend listener) {
        // TODO
    }

    /**
     * Adds a user to the Users collection in Firestore.
     *
     * @param user     The user to be added.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void addUser(User user, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = usersRef.document();
        user.setId(newDocRef.getId());
        newDocRef.set(user.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }


    /**
     * Updates a user in the Users collection in Firestore.
     *
     * @param user     The updated user.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void updateUser(User user, FirestoreCallbackSend listener) {
        usersRef.document(user.getId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Fetches users from the Users collection in Firestore and notifies the listener.
     *
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchUsers(Consumer<Query> filter, FirestoreCallbackUsersReceive listener) {
        CollectionReference ref = usersRef;
        filter.accept(ref);
        ref.get().addOnSuccessListener(task -> {
            List<User> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : task) {
                Map<String, Object> data = document.getData();
                User user = User.fromMap(document.getId(), data);
                items.add(user);
            }
            listener.onDataReceived(items);
        }).addOnFailureListener(listener::onError);
    }

    /**
     * Fetches users with the given device id from the Users collection in Firestore
     * and notifies the listener.
     *
     * @param deviceId The device id to search for.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchUsersByDeviceId(String deviceId, FirestoreCallbackUsersReceive listener) {
        fetchUsers(f -> f.whereEqualTo("deviceId", deviceId), listener);
    }

    /**
     * Deletes a user's profile if they are not organizing any events.
     *
     * @param user     The user whose profile is to be deleted.
     * @param listener The listener to be notified of success or failure.
     */
    public void deleteUser(User user, FirestoreCallbackSend listener) {
        // Prevent if the user is an ACTIVE organizer
        eventsRef.whereEqualTo("organizer_id", user.getId())
                .whereGreaterThanOrEqualTo("end", DateUtil.toLong(LocalDateTime.now()))
                .limit(1)
                .get()
                .addOnSuccessListener(eventQuery -> {
                    // Fail fast if user is an organizer
                    if (!eventQuery.isEmpty()) {
                        listener.onSendFailure(new IllegalArgumentException("Cannot delete profile while organizing an event."));
                        return;
                    }

                    // Query all applicants where the user id matches
                    applicantsRef.whereEqualTo("user_id", user.getId())
                            .get()
                            .addOnSuccessListener(applicantQuery -> {

                                // Query all comments where the userid matches
                                commentsRef.whereEqualTo("user_id", user.getId())
                                        .get()
                                        .addOnSuccessListener(commentQuery -> {

                                            // Batch deletion to ensure atomicity
                                            WriteBatch batch = db.batch();

                                            // Batch applicants
                                            for (QueryDocumentSnapshot snapshot : applicantQuery) {
                                                batch.delete(snapshot.getReference());
                                            }

                                            // Batch comments
                                            for (QueryDocumentSnapshot snapshot : commentQuery) {
                                                batch.delete(snapshot.getReference());
                                            }

                                            // Batch actual user
                                            batch.delete(usersRef.document(user.getId()));

                                            // Commit
                                            batch.commit()
                                                    .addOnSuccessListener(listener::onSendSuccess)
                                                    .addOnFailureListener(listener::onSendFailure);
                                        })
                                        .addOnFailureListener(listener::onSendFailure);
                            }).addOnFailureListener(listener::onSendFailure);
                });
    }

    // TODO: Needs testing
    public void fetchApplicantStatus(User user, Event event, FirestoreCallbackApplicantsReceive listener) {
        applicantsRef.whereEqualTo("user_id", user.getId())
                .whereEqualTo("event_id", event.getId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Applicant> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Applicant applicant = Applicant.fromMap(doc.getId(), doc.getData());
                        list.add(applicant);
                    }
                    listener.onDataReceived(list);
                })
                .addOnFailureListener(listener::onError);
    }
}