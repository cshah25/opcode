package com.example.opcodeapp.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.opcodeapp.BuildConfig;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.enums.LoginState;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.DeviceIdUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Singleton class to keep track of the currently logged in user.
 */
public class SessionController {
    private static SessionController instance;

    private Call currentCall;

    private final OkHttpClient client = new OkHttpClient();

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

                if (current_user == null) {
                    Log.w("SessionController", "No user found for device id");
                    state.postValue(LoginState.LOGGED_OUT);
                    return;
                }
                state.postValue(LoginState.LOGGED_IN);
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

        if (current_user == null) {
            return null;
        } else {
            if (current_user.getLatitude() == 0L && current_user.getLongitude() == 0L)
                fetchUserLocation();

            return current_user;

        }
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

    private void fetchUserLocation() {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.geoapify.com")
                .addPathSegment("v1")
                .addPathSegment("ipinfo")
                .addQueryParameter("format", "json")
                .addQueryParameter("apiKey", BuildConfig.GEOAPIFY_GEOLOCATION_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("LocationDebug", "request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e("LocationDebug", "request unsuccessful");
                    return;
                }

                parseLocation(body);
            }
        });
    }

    public void parseLocation(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject location = root.optJSONObject("location");
            if (location != null) {
                double lat = location.optDouble("latitude");
                double lon = location.optDouble("longitude");

                if (current_user != null) {
                    if (lat != current_user.getLatitude() || lon != current_user.getLongitude()) {
                        current_user.setLatitude(lat);
                        current_user.setLongitude(lon);

                    }
                }

                if (current_user.isDirty()) {
                    UserRepository user_repo = new UserRepository(FirebaseFirestore.getInstance());
                    user_repo.updateUser(current_user, new FirestoreCallbackSend() {
                        @Override
                        public void onSendSuccess(Void aVoid) {
                            current_user.setDirty(false);
                        }

                        @Override
                        public void onSendFailure(Exception e) {
                            Log.e("LocationDebug", "request failed", e);

                        }
                    });
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
