package com.example.opcodeapp.controller;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
     * Handles notifications when app is in background
     * @param remoteMessage Remote message that has been received.
     */
    @Override
    // nonnull because android studio said so
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // display the notification real quick
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "default")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.ic_notifications_active)
                        .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}
