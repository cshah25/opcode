package com.example.opcodeapp;

import android.os.Bundle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizersEventCreation extends Fragment {

    private EditText name;
    private EditText date;
    private EditText location;
    private EditText limit;
    private EditText close;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizers_event_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name     = view.findViewById(R.id.event_creation_name_input);
        date     = view.findViewById(R.id.event_creation_date_time_input);
        location = view.findViewById(R.id.event_creation_location_input);
        limit    = view.findViewById(R.id.event_creation_wait_list_input);
        close    = view.findViewById(R.id.event_creation_close_input);

        Button createButton = view.findViewById(R.id.event_creation_button);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        createButton.setOnClickListener(v -> {
            String eventName     = name.getText().toString();
            String eventLocation = location.getText().toString();
            // TODO: limit is not yet stored in the Event model
            LocalDate startDate  = LocalDate.parse(date.getText().toString(), fmt);
            LocalDateTime regEnd = LocalDate.parse(close.getText().toString(), fmt).atStartOfDay();

            User organizer = SessionController.getInstance(requireContext()).getCurrentUser();

            Event newEvent = new Event(eventName, eventLocation, "", startDate, LocalDateTime.now(), startDate, regEnd, organizer, 0f);

            DBManager db = new DBManager(FirebaseFirestore.getInstance());
            db.addEvent(newEvent, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess() {
                    Toast.makeText(getContext(), "Event created!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
