package com.example.opcodeapp.repository;

import androidx.annotation.Nullable;

import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Repository class for {@link Event} objects. Handles creation, fetching, deletion and updating of
 * Event objects within the Firebase database
 */
public class EventRepository extends Repository {

    /**
     * Constructor for EventRepository
     */
    public EventRepository(FirebaseFirestore db) {
        super(db, "Events");
    }

    /**
     * Adds a new event to the Events collection in Firestore. This will modify the id to a newly
     * created Firebase document
     *
     * @param event    The event to be added.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void addEvent(Event event, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = ref.document();
        event.setId(newDocRef.getId());
        newDocRef.set(event.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Fetches a single {@link Event} from the Events collection and notifies the listener
     *
     * @param id       The Firestore document id of the Event
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchEvent(String id, FirestoreCallbackEventReceive listener) {
        ref.document(id).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || doc.getData() == null) {
                        listener.onError(new IllegalArgumentException("Event not found"));
                        return;
                    }
                    Event event = Event.fromMap(doc.getId(), doc.getData());
                    listener.onDataReceived(event);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches a collection of {@link Event} objects with the given organizer id from the Events
     * collection and notifies the listener.
     *
     * @param organizerId The organizer id to search for
     * @param listener    The listener to be notified of the success or failure of the operation.
     */
    public void fetchEventsByOrganizerId(String organizerId, FirestoreCallbackEventsReceive listener) {
        ref.whereEqualTo("organizer_id", organizerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event event = Event.fromMap(doc.getId(), doc.getData());
                        items.add(event);
                    }
                    listener.onDataReceived(items);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches a collection of {@link Event} objects from the Events collection and notifies the listener.
     *
     * @param filter   The filters applied to specify the query's parameters
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchEvents(@Nullable Function<Query, Query> filter, FirestoreCallbackEventsReceive listener) {
        Query query = (filter == null) ? ref : filter.apply(ref);
        query.get().addOnSuccessListener(snapshot -> {
            List<Event> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot) {
                Event event = Event.fromMap(document.getId(), document.getData());
                if (event != null)
                    items.add(event);
            }
            listener.onDataReceived(items);
        }).addOnFailureListener(listener::onError);
    }

    /**
     * Updates an event in the Events collection in Firestore.
     *
     * @param event    The updated event
     * @param listener The listener to be notified of the success or failure of the operation
     */
    public void updateEvent(Event event, FirestoreCallbackSend listener) {
        ref.document(event.getId())
                .set(event.toMap(), SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Deletes an event by the document's id
     *
     * @param id       The Firestore id of the event
     * @param listener The listener to be notified of success or failure.
     */
    public void deleteEvent(String id, FirestoreCallbackSend listener) {


        ref.document(id)
                .delete()
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);


    }

    /**
     * Delete all Event documents that matches a given organizer's id
     *
     * @param organizerId The Firestore id of the User organizing the event
     * @param listener    The listener to be notified of success or failure
     */
    public void deleteEventsByOrganizerId(String organizerId, FirestoreCallbackSend listener) {
        ref.whereEqualTo("organizer_id", organizerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onSendSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(listener::onSendSuccess)
                            .addOnFailureListener(listener::onSendFailure);
                })
                .addOnFailureListener(listener::onSendFailure);

    }
}
