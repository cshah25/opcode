package com.example.opcodeapp.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.opcodeapp.MainActivity;
import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Controls notifications so they always appear (even if app is in background) and handles
 * updating FCM tokens.
 */
public class NotificationController extends FirebaseMessagingService {
    /**
     * Handles updates to user's fcm token
     * @param token The token used for sending messages to this application instance. This token is
     *     the same as the one retrieved by {@link FirebaseMessaging#getToken()}.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        User cur = SessionController.getInstance(getApplicationContext()).getCurrentUser();
        if (cur != null) {
            updateToken(cur, token);
        }
    }

    /**
     * Convenience method to update the fcm token for a particular user
     * @param user: user to update
     * @param token: token to put in the user
     */
    public static void updateToken(User user, String token) {
        UserRepository repository = new UserRepository(FirebaseFirestore.getInstance());

        user.setFcmToken(token);
        repository.updateUser(user, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                Log.i("NotificationController", "Updated fcm token");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.e("NotificationController", String.format("Could not update fcm token: %s", e.toString()));
            }
        });
    }

    /**
     * Handles notifications when app is in background (or foreground)
     * @param remoteMessage Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String eventId = remoteMessage.getData().get("eventId");
        String destination = remoteMessage.getData().get("destination");
        String notiId = remoteMessage.getData().get("notiId");
        Log.d("NotificationController", "Received noti: " + title + ", " + body + ", " + eventId + ", " + destination + ", " + notiId);

        if (title == null || body == null || eventId == null || destination == null || notiId == null) {
            Log.w("NotificationController", "Missing data");
            return;
        }

        // the intents are here so the user is taken to the event when they click on the noti
        // or to the notifications page if the noti isn't really relevant to a particular event.
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("noti_id", notiId);
        if (destination.equals("event_detail")) {
            intent.putExtra("destination", "details");
            intent.putExtra("event_id", eventId);
        } else if (destination.equals("notifications")) {
            intent.putExtra("destination", "notifications");
        } else {
            Log.e("NotificationController", "no destination");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // display the notification real quick
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "default")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.ic_notifications_active)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
