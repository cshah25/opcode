package com.example.opcodeapp.repository;

import androidx.annotation.Nullable;

import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.callback.FirestoreCallbackUsersReceive;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Repository class for {@link User} objects. Handles creation, fetching, deletion and updating of
 * User objects within the Firebase database
 */
public class UserRepository extends Repository {

    /**
     * Constructor for UserRepository
     */
    public UserRepository(FirebaseFirestore db) {
        super(db, "Users");
    }

    /**
     * Adds a new user to the Users collection in Firestore. This will modify the id to a newly
     * created Firebase document
     *
     * @param user     The user to be added.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void addUser(User user, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = ref.document();
        user.setId(newDocRef.getId());
        newDocRef.set(user.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Fetches a single {@link User} object from the Users collection and notifies the listener
     *
     * @param id       The filters applied to specify the query
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchUser(String id, FirestoreCallbackUserReceive listener) {
        ref.document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || doc.getData() == null) {
                        listener.onError(new IllegalArgumentException("User not found"));
                        return;
                    }
                    User user = User.fromMap(doc.getId(), doc.getData());
                    listener.onDataReceived(user);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches a single {@link User} object with the given device id from the Users collection and
     * notifies the listener.
     *
     * @param deviceId The device id to search for.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchUserByDeviceId(String deviceId, FirestoreCallbackUserReceive listener) {
        ref.whereEqualTo("device_id", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onDataReceived(null);
                        return;
                    }
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) snapshot.getDocuments().get(0);
                    User user = User.fromMap(doc.getId(), doc.getData());
                    listener.onDataReceived(user);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Fetches a collection of {@link User} objects from the Users collection and notifies the listener.
     *
     * @param filter   The filters applied to specify the query
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void fetchUsers(@Nullable Function<Query, Query> filter, FirestoreCallbackUsersReceive listener) {
        Query query = (filter == null) ? ref : filter.apply(ref);
        query.get().addOnSuccessListener(task -> {
            List<User> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : task) {
                Map<String, Object> data = document.getData();
                User user = User.fromMap(document.getId(), data);
                if (user != null)
                    items.add(user);
            }
            listener.onDataReceived(items);
        }).addOnFailureListener(listener::onError);
    }

    /**
     * Updates a user in the Users collection in Firestore.
     *
     * @param user     The updated user.
     * @param listener The listener to be notified of the success or failure of the operation.
     */
    public void updateUser(User user, FirestoreCallbackSend listener) {
        ref.document(user.getId())
                .set(user.toMap(), SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Deletes a user's profile by the given id.
     *
     * @param id       The id of the user to be deleted.
     * @param listener The listener to be notified of success or failure.
     */
    public void deleteUser(String id, FirestoreCallbackSend listener) {
        ref.document(id)
                .delete()
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }
}
