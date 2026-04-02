package com.example.opcodeapp.controller;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.enums.LoginState;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.DeviceIdUtil;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton class to keep track of the currently logged in user.
 */
public class SessionController {
    private static SessionController instance;

    private User current_user;
    private final MutableLiveData<LoginState> state = new MutableLiveData<>(LoginState.PENDING);

    private SessionController(Context context) {
        UserRepository repository = new UserRepository(FirebaseFirestore.getInstance());
        String deviceId = DeviceIdUtil.getDeviceId(context);

        repository.fetchUserByDeviceId(deviceId, new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                Log.i("SessionController", "received query result ok");
                current_user = user;

                if (current_user != null) {
                    state.postValue(LoginState.LOGGED_IN);
                    return;
                }

                Log.e("SessionController", "No user found for device id");
                state.postValue(LoginState.LOGGED_OUT);
            }

            @Override
            public void onError(Exception e) {
                Log.e("SessionController", "received query error", e);
                current_user = null;
                state.postValue(LoginState.LOGGED_OUT);
            }
        });
    }

    public static SessionController getInstance(Context context) {
        if (instance == null) {
            instance = new SessionController(context.getApplicationContext());
        }
        return instance;
    }

    public User getCurrentUser() {
        return current_user;
    }

    public void setCurrentUser(User current_user) {
        this.current_user = current_user;
    }

    public boolean isLoggedIn() {
        return current_user != null;
    }

    public void logout() {
        current_user = null;
        state.postValue(LoginState.LOGGED_OUT);
    }

    public LiveData<LoginState> getLoginState() {
        return state;
    }
}
