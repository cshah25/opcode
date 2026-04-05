package com.example.opcodeapp.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.Notification;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

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
        RequestBody form_body = new FormBody.Builder()
                .add("userId", notification.getUser_id())
                .add("body", notification.getBody())
                .add("eventId", notification.getEvent_id())
                .build();

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("beaconbrigade.ca")
                .addPathSegment("opcode")
                .addPathSegment("bs")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .put(form_body)
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

//    /**
//     * Updates a notification
//     * @param notification: notification to update (uses the id, so ensure those match)
//     * @param listener: respond to success/failure of updating notification
//     */
//    // probably this function isn't needed
//    public void updateNotification(Notification notification, FirestoreCallbackSend listener) {
//        ref.document(notification.getId())
//                .set(notification.toMap(), SetOptions.merge())
//                .addOnSuccessListener(listener::onSendSuccess)
//                .addOnFailureListener(listener::onSendFailure);
//        // TODO: add request to server to send new notification and remove old one
//        // .     although seems like this function isn't needed
//    }
}
