package com.example.opcodeapp;


import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertEquals;



import android.util.Log;


import com.example.opcodeapp.callback.FirestoreCallbackSend;

import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.User;

import com.example.opcodeapp.repository.UserRepository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DBManagerTest {

    private UserRepository userRepository;

    @Before
    public void setup() {
        // 1. Initialize DBManager with the mocked database



        userRepository = new UserRepository(FirebaseFirestore.getInstance());
    }

    @Test
    public void testAddUser() {

        User.Builder b = User.builder()
                .name("mock_user")
                .email("mock_user@ualberta.ca")
                .phoneNum("676767676767");

        User user = b.build();


        // EXECUTE
        userRepository.addUser(user, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("TEST", "Success");
            }

            @Override
            public void onSendFailure(Exception e) {

                Log.d("TEST", "Failure");

            }
        });

        // ASSERT & VERIFY
        assertNotNull(user.getId()); // Check if ID was set on user object

    }

    @Test
    public void testUpdateUser() {
        User.Builder b = User.builder()
                .name("mock_user_2")
                .email("mock_user_2@ualberta.ca")
                .phoneNum("686868686868686");

        User mock_user = b.build();

        userRepository.addUser(mock_user, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("TEST", "Success");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.d("TEST", "Failure");
            }

        });

        mock_user.setName("mock_user_3");


        userRepository.updateUser(mock_user, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("TEST", "Success");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.d("TEST", "Failure");
            }
        });

        userRepository.fetchUser(mock_user.getId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                assertEquals(user.getName(), "mock_user_3");
            }

            @Override
            public void onError(Exception e) {
                Log.d("TEST", "Failure");
            }
        });






    }

}



