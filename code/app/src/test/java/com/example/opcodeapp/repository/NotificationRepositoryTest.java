package com.example.opcodeapp.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.opcodeapp.callback.FirestoreCallbackNotificationsReceive;
import com.example.opcodeapp.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class NotificationRepositoryTest {

    @Test
    void fetchNotificationsByUserId() {
        // curl -d 'userId=johntester123' -d 'body=test1' -d 'eventId=testEvent' https://beaconbrigade.ca/opcode/bssend-notification/
        NotificationRepository repo = new NotificationRepository(FirebaseFirestore.getInstance());

        // this lets us do async tests
        CountDownLatch latch = new CountDownLatch(1);


        repo.fetchNotificationsByUserId("johntester123", new FirestoreCallbackNotificationsReceive() {
            @Override
            public void onDataReceived(List<Notification> items) {
                assertEquals(2, items.size());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                assert(false);
            }
        });

        // makes sure the test waits on the network request
        // I didn't actually check if the test worked without the latch, but it works with it so :)
        try {
            assert(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            assert(false);
        }
    }
}