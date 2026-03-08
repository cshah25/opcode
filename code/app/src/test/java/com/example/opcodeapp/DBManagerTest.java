package com.example.opcodeapp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.Context;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DBManagerTest {

    @Mock private FirebaseFirestore mockDb;
    @Mock private CollectionReference mockCollection;
    @Mock private DocumentReference mockDocRef;
    @Mock private Task<Void> mockTask;
    @Mock private FirestoreCallbackSend mockListener;

    private DBManager dbManager;

    @Before
    public void setup() {
        // 1. Initialize DBManager with the mocked database

        MockitoAnnotations.openMocks(this);


        // 2. Mock the chain: db.collection("Users").document()
        when(mockDb.collection("Users")).thenReturn(mockCollection);
        when(mockCollection.document()).thenReturn(mockDocRef);

        // 3. Mock the ID generation
        when(mockDocRef.getId()).thenReturn("mock_id_123");

        // 4. Mock the .set(user) call to return a task
        when(mockDocRef.set(any(User.class))).thenReturn(mockTask);

        dbManager = new DBManager(mockDb);
    }

    @Test
    public void testAddUser() {

        User user = new User("Vedant Patel", "vspatel1@ualberta.ca", "67676767");

        // 5. Tell the mockTask to trigger the Success Listener immediately
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null); // Manually trigger the success callback
            return mockTask;
        });

        // Ensure the chain doesn't break if onFailure is called
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        // EXECUTE
        dbManager.addUser(user, mockListener);

        // ASSERT & VERIFY
        assertEquals("mock_id_123", user.getId()); // Check if ID was set on user object
        verify(mockListener).onSendSuccess();      // Verify our callback was reached
        verify(mockListener, never()).onSendFailure(any());
    }

    @Test
    public void testUpdateUser() {
        User newUser = new User("John Doe", "blah@gmail.com", "98372042");
    }

}



