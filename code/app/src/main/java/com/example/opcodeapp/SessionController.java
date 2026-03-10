package com.example.opcodeapp;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Simpleton class to keep track of the currently logged in user.
 * Use SessionController.getInstance(ctx).getCurrentUser() to the
 * get the currently logged in user.
 */
public class SessionController {
    private static SessionController instance;
    private User current_user;
    private MutableLiveData<LoginState> state = new MutableLiveData<>(LoginState.PENDING);

    public SessionController(Context context) {
        String id = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        DBManager db = new DBManager(FirebaseFirestore.getInstance());
        db.fetchUserByDeviceId(id, new FirestoreCallbackUsersReceive() {
            @Override
            public void onDataReceived(List<User> items) {
                Log.i("SessionController", "received query result ok");
                if (!items.isEmpty()) {
                    current_user = items.get(0);
                    state.postValue(LoginState.LOGGED_IN);
                } else {
                    state.postValue(LoginState.LOGGED_OUT);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("SessionController", String.format("received query error: %s", e));
                state.postValue(LoginState.LOGGED_OUT);
            }
        });
    }

    public static SessionController getInstance(Context context) {
        if (instance == null) {
            instance = new SessionController(context);
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
        return this.current_user != null;
    }

    public void logout() {
        current_user = null;
    }

    public LiveData<LoginState> getLoginState() {
        return state;
    }
}
