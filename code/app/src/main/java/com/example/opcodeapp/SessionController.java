package com.example.opcodeapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.opcodeapp.model.User;
import com.example.opcodeapp.util.DeviceIdUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private FirebaseAuth mAuth;
    private FirebaseUser fire_user;

    public SessionController(Context context) {
        String id = DeviceIdUtil.getDeviceId(context);
        mAuth = FirebaseAuth.getInstance();
        fire_user = mAuth.getCurrentUser();
        DBManager db = new DBManager(FirebaseFirestore.getInstance());
        db.fetchUserByDeviceId(id, new FirestoreCallbackUsersReceive() {
            @Override
            public void onDataReceived(List<User> items) {
                Log.i("SessionController", "received query result ok");
                if (!items.isEmpty()) {
                    current_user = items.get(0);
                    if (fire_user == null || !fire_user.getEmail().equals(current_user.getEmail())) {
                        mAuth.signInWithEmailAndPassword(current_user.getEmail(), current_user.getDeviceId())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.i("SessionController", "Logged fire in");
                                            fire_user = mAuth.getCurrentUser();
                                        } else {
                                            Log.e("SessionController", "Couldn't fire base log in");
                                        }
                                    }
                                });
                    }
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

    public void updateFireAuth() {
        fire_user = mAuth.getCurrentUser();
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
        mAuth.signOut();
        state.postValue(LoginState.LOGGED_OUT);
    }

    public LiveData<LoginState> getLoginState() {
        return state;
    }
}
