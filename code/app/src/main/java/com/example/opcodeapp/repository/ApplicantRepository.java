package com.example.opcodeapp.repository;

import androidx.annotation.Nullable;

import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
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
 * Repository class for {@link Applicant} objects. Handles creation, fetching, deletion and updating of
 * Applicant objects within the Firebase database
 */
public class ApplicantRepository extends Repository {

    /**
     * Constructor for ApplicantRepository
     */
    public ApplicantRepository(FirebaseFirestore db) {
        super(db, "Applicants");
    }

    /**
     * Adds a new applicant to the Applicants collection in Firestore. This will modify the id to a newly
     * created Firebase document
     *
     * @param applicant The applicant to be added
     * @param listener  The listener to be notified of the success or failure of the operation
     */
    public void addApplicant(Applicant applicant, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = ref.document();
        applicant.setId(newDocRef.getId());
        newDocRef.set(applicant.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Fetches a single {@link Applicant} object from the Applicants collection and notifies the listener.
     *
     * @param userId   The user id of the applicant
     * @param eventId  The event id the applicant is attending
     * @param listener The listener to be notified of the success or failure of the operation
     */
    public void fetchApplicant(String userId, String eventId, FirestoreCallbackApplicantReceive listener) {
        ref.whereEqualTo("user_id", userId)
                .whereEqualTo("event_id", eventId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onError(new IllegalArgumentException("No matching users found"));
                        return;
                    }
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) snapshot.getDocuments().get(0);
                    Applicant applicant = Applicant.fromMap(doc.getId(), doc.getData());
                    listener.onDataReceived(applicant);
                })
                .addOnFailureListener(listener::onError);
    }


    /**
     * Fetches a collection of {@link Applicant} objects from the Applicants collection and notifies the listener.
     *
     * @param filter   The filters applied to specify the query's parameters
     * @param listener The listener to be notified of the success or failure of the operation
     */
    public void fetchApplicants(@Nullable Function<Query, Query> filter, FirestoreCallbackApplicantsReceive listener) {
        Query query = (filter == null) ? ref : filter.apply(ref);
        query.get().addOnSuccessListener(snapshot -> {
            List<Applicant> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot) {
                Applicant applicant = Applicant.fromMap(document.getId(), document.getData());
                if (applicant != null)
                    items.add(applicant);
            }
            listener.onDataReceived(items);
        }).addOnFailureListener(listener::onError);
    }

    /**
     * Retrieves all applicants that are related to the given event id
     *
     * @param eventId  The document id of the event
     * @param listener The listener to be notified of the success or failure of the operation
     */
    public void fetchApplicantsByEvent(String eventId, FirestoreCallbackApplicantsReceive listener) {
        fetchApplicants(q -> q.whereEqualTo("event_id", eventId), listener);
    }

    /**
     * Retrieves all applicants that are related to the given user id
     *
     * @param userId   The document id of the event
     * @param listener The listener to be notified of the success or failure of the operation
     */
    public void fetchApplicantsByUser(String userId, FirestoreCallbackApplicantsReceive listener) {
        fetchApplicants(q -> q.whereEqualTo("user_id", userId), listener);
    }

    /**
     * Updates a applicant in the Applicants collection in Firestore.
     *
     * @param applicant The updated applicant
     * @param listener  The listener to be notified of the success or failure of the operation
     */
    public void updateApplicant(Applicant applicant, FirestoreCallbackSend listener) {
        ref.document(applicant.getId())
                .set(applicant.toMap(), SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Deletes ab even by the Firestore id.
     *
     * @param id       The Firestore id of the applicant
     * @param listener The listener to be notified of success or failure.
     */
    public void deleteApplicant(String id, FirestoreCallbackSend listener) {
        ref.document(id)
                .delete()
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Delete all Applicant documents that matches a given organizer's id
     *
     * @param userId   The Firestore id of the User organizing the applicant
     * @param listener The listener to be notified of success or failure
     */
    public void deleteApplicantsByUser(String userId, FirestoreCallbackSend listener) {
        ref.whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onSendSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshot)
                        batch.delete(doc.getReference());

                    batch.commit()
                            .addOnSuccessListener(listener::onSendSuccess)
                            .addOnFailureListener(listener::onSendFailure);
                })
                .addOnFailureListener(listener::onSendFailure);

    }

    /**
     * Delete all Applicant documents that matches a given event's id
     *
     * @param eventId  The Firestore id of the Event the applicant is attending
     * @param listener The listener to be notified of success or failure
     */
    public void deleteApplicantsByEvent(String eventId, FirestoreCallbackSend listener) {
        ref.whereEqualTo("event_id", eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onSendSuccess(null);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshot)
                        batch.delete(doc.getReference());

                    batch.commit()
                            .addOnSuccessListener(listener::onSendSuccess)
                            .addOnFailureListener(listener::onSendFailure);
                })
                .addOnFailureListener(listener::onSendFailure);

    }

    public void fetchApplicantsByStatus(Event event, ApplicantStatus status, FirestoreCallbackApplicantsReceive listener) {
        ref.whereEqualTo("event_id", event.getId())
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Applicant> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Applicant applicant = Applicant.fromMap(doc.getId(), doc.getData());
                        if (applicant != null)
                            list.add(applicant);
                    }
                    listener.onDataReceived(list);
                })
                .addOnFailureListener(listener::onError);
    }
}
