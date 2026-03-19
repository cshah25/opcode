package com.example.opcodeapp.view;

import static android.view.View.GONE;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackSend;
import com.example.opcodeapp.LotterySystem;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventInvitationFragment#newInstance} factory method to
 * create an instance of this fragment. This is for accepting/rejecting an invite to an Event
 */
public class EventInvitationFragment extends Fragment {

    private Event event;
    private DBManager db;

    public EventInvitationFragment() {
        db = new DBManager(FirebaseFirestore.getInstance());
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param e The event to be invited to
     * @return A new instance of fragment EventInvitationFragment.
     */
    public static EventInvitationFragment newInstance(Event e) {
        EventInvitationFragment fragment = new EventInvitationFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", e);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = getArguments().getParcelable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_event_invitation, container, false);

        TextView title = v.findViewById(R.id.invitation_event_name);
        title.setText(event.getName());
        TextView date = v.findViewById(R.id.invitation_date_time);
        date.setText(String.format("Starts: %s", event.getStart().toString()));
        TextView location = v.findViewById(R.id.invitation_location);
        location.setText(String.format("Location: %s", event.getLocation()));
        TextView registration_close = v.findViewById(R.id.invitation_registration_closes);
        registration_close.setText(String.format("Registration closes on %s", event.getRegistrationEnd().toString()));
        TextView price = v.findViewById(R.id.invitation_price);
        price.setText(String.valueOf(event.getPrice()));

        TextView entrants = v.findViewById(R.id.invitation_waiting_list_size);
        // android studio was complaining about the locale here
        entrants.setText(String.format(Locale.getDefault(), "%d waiting to join", event.getInitialApplicants().size()));

        User cur = SessionController.getInstance(getContext()).getCurrentUser();
        Button accept = v.findViewById(R.id.invitation_accept_button);
        accept.setOnClickListener(view -> {
            event.setAttendee(cur);
            db.updateEvent(event, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess() {
                    Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error accepting invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        });
        Button decline = v.findViewById(R.id.invitation_decline_button);
        decline.setOnClickListener(view -> {
            event.setDeclined(cur);
            LotterySystem l = new LotterySystem();
            User n = l.drawReplacement(event);
            ArrayList<User> list = new ArrayList<>();
            list.add(n);
            event.setInvited(list);
            db.updateEvent(event, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess() {
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error declining invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        });

        ApplicantStatus status = event.getApplicantStatus(SessionController.getInstance(getContext()).getCurrentUser());
        if (status != ApplicantStatus.NOT_DRAWN) {
            accept.setVisibility(GONE);
            decline.setVisibility(GONE);
        }

        return v;
    }
}