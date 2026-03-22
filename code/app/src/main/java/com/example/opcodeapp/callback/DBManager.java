package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

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