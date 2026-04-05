package com.example.opcodeapp.repository;

import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * Repository for interacting with notifications
 */
public class NotificationRepository extends Repository {
    /**
     * Constructor for the NotificationRepository class
     *
     * @param db    The firebase firestore instance
     */
    public NotificationRepository(FirebaseFirestore db) {
        super(db, "Notifications");
    }

    /**
     * Creates a notification on the database side.
     * @param notification: the notification to add
     * @param listener: respond to success/failure of adding notification
     */
    public void addNotification(Notification notification, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = ref.document();
        notification.setId(newDocRef.getId());
        newDocRef.set(notification.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    /**
     * Updates a notification
     * @param notification: notification to update (uses the id, so ensure those match)
     * @param listener: respond to success/failure of updating notification
     */
    public void updateNotification(Notification notification, FirestoreCallbackSend listener) {
        ref.document(notification.getId())
                .set(notification.toMap(), SetOptions.merge())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }
}
