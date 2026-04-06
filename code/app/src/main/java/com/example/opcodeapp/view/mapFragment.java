package com.example.opcodeapp.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.opcodeapp.BuildConfig;
import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * The fragment for showing the Event information (Organizer Perspective) after registration is complete.
 */
public class mapFragment extends Fragment {

    private Call currentCall;

    private ImageView mapView;

    private final OkHttpClient client = new OkHttpClient();
    private Event event;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            throw new IllegalArgumentException("No arguments passed");

        event = args.getParcelable("event", Event.class);
        if (event == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

        applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {

            @Override
            public void onDataReceived(List<Applicant> applicants) {

                getUsersLocation(applicants);

            }

            @Override
            public void onError(Exception e) {
                Log.e("mapFragment", "Error fetching applicants", e);
            }

        });
        mapView = view.findViewById(R.id.map_view);
    }


    /**
     * Gets the User instances for each applicant and calls a function to create an api request for map.
     * @param applicants
     * list of applicants of an event whose locations will be displayed on the map.
     */
    public void getUsersLocation(List<Applicant> applicants) {
        List<User> users = new ArrayList<>();

        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
        final int total = applicants.size();
        final AtomicInteger counter = new AtomicInteger(0);

        for (Applicant applicant : applicants) {
            userRepository.fetchUser(applicant.getUserId(), new FirestoreCallbackUserReceive() {
                /**
                 * receives the user information and adds it to the data list.
                 * @param user
                 * the user to add to the data list.
                 */
                @Override
                public void onDataReceived(User user) {
                    users.add(user);

                    // Check if this was the last user to be fetched
                    if (counter.incrementAndGet() == total) {
                        constructRequest(users); // Call a separate helper to write the file
                    }
                }

                /**
                 * if the user is not found then they are skipped.
                 * @param e
                 * exception to handle.
                 */
                @Override
                public void onError(Exception e) {
                    if (counter.incrementAndGet() == total) {
                        constructRequest(users);
                    }
                }
            });
        }
    }

    //https://maps.geoapify.com/v1/staticmap?style=osm-bright-smooth&width=600&height=400&center=lonlat%3A-122.29009844646316%2C47.54607447032754&zoom=14.3497&marker=lonlat%3A-122.29188334609739%2C47.54403990655936%3Btype%3Aawesome%3Bcolor%3A%23bb3f73%3Bsize%3Ax-large%3Bicon%3Apaw%7Clonlat%3A-122.29282631194182%2C47.549609195001494%3Btype%3Amaterial%3Bcolor%3A%234c905a%3Bicon%3Atree%3Bicontype%3Aawesome%7Clonlat%3A-122.28726954893025%2C47.541766557545884%3Btype%3Amaterial%3Bcolor%3A%234c905a%3Bicon%3Atree%3Bicontype%3Aawesome&apiKey=75386e4b50524f018ae80fa506253302

    /**
     * takes the user data in csv format and saves it to the downloads folder.
     * @param data
     * Each row in the array represents a user with their name, email, and phone number.
     */
    private void constructRequest(List<User> users) {

        String marker_string = "";

        for (User u: users) {

            if (marker_string.equals("")) {
                marker_string += "lonlat:" + u.getLongitude() + "," + u.getLatitude() + ";type:material;color:#4c905a;icon:person;icontype:awesome";
            } else {
                marker_string += "|lonlat:" + u.getLongitude() + "," + u.getLatitude() + ";type:material;color:#4c905a;icon:person;icontype:awesome";
            }



        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.geoapify.com")
                .addPathSegment("v1")
                .addPathSegment("staticmap")
                .addQueryParameter("style", "osm-bright-smooth")
                .addQueryParameter("width", String.valueOf(screenWidth))
                .addQueryParameter("height", String.valueOf(screenHeight))
                // The marker string is complex, but the builder handles the ';' and '#' symbols
                .addQueryParameter("marker", marker_string)
                .addQueryParameter("apiKey", BuildConfig.GEOAPIFY_GEOLOCATION_API_KEY)
                .build();


        Glide.with(this).load(url.toString()).into(mapView);
    }





    /**
     * Called when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
