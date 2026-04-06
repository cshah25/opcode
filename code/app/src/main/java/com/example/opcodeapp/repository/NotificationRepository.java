package com.example.opcodeapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.opcodeapp.callback.FirestoreCallbackNotificationReceive;
import com.example.opcodeapp.callback.FirestoreCallbackNotificationsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Repository for interacting with notifications
 */
public class NotificationRepository extends Repository {
    OkHttpClient client;

    /**
     * Constructor for the NotificationRepository class
     *
     * @param db    The firebase firestore instance
     */
    public NotificationRepository(FirebaseFirestore db) {
        super(db, "Notifications");
        client = new OkHttpClient();
    }

    /**
     * Creates a notification on the database side.
     * @param notification: the notification to add
     * @param listener: respond to success/failure of adding notification
     */
    public void addNotification(Notification notification, FirestoreCallbackSend listener) {
        // send request to noti-server which will handle adding the notification to the
        // firestore (which we could do here) and send the actual notification to the device,
        // which is the whole point of the server.
        Log.i("NotificationRepository", "noti" + notification + notification.getBody() + notification.getId());
        RequestBody form_body = new FormBody.Builder()
                .add("userId", notification.getUser_id())
                .add("body", notification.getBody())
                .add("eventId", notification.getEvent_id())
                .add("destination", notification.getDestination())
                .build();

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("beaconbrigade.ca")
                .addPathSegment("opcode")
                .addPathSegment("bssend-notification")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(form_body)
                .build();

        Call current_call = client.newCall(request);
        current_call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("NotificationRepository", "api request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("NotificationRepository", String.format("got response: %s", response));
            }
        });
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

    /**
     * Fetches all of the notifications associated with a user
     * @param user_id: the user to fetch for
     * @param callback: respond to the fetched notis/errors
     */
    public void fetchNotificationsByUserId(String user_id, FirestoreCallbackNotificationsReceive callback) {
        ref.whereEqualTo("userId", user_id)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Notification> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Notification event = Notification.fromMap(doc.getId(), doc.getData());
                        items.add(event);
                    }
                    callback.onDataReceived(items);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Delete a notification
     * @param noti_id: id of the notification
     * @param callback: respond to result of this operation
     */
    public void deleteNotification(String noti_id, FirestoreCallbackSend callback) {
        ref.document(noti_id)
                .delete()
                .addOnSuccessListener(callback::onSendSuccess)
                .addOnFailureListener(callback::onSendFailure);
    }

    public void fetchNotification(String noti_id, FirestoreCallbackNotificationReceive callback) {
        ref.document(noti_id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || doc.getData() == null) {
                        callback.onDataReceived(null);
                        return;
                    }
                    Notification noti = Notification.fromMap(doc.getId(), doc.getData());
                    callback.onDataReceived(noti);
                })
                .addOnFailureListener(callback::onError);
    }
}
