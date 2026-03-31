package com.example.opcodeapp.view;

import static android.view.View.GONE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventInvitationFragment#newInstance} factory method to
 * create an instance of this fragment. This is for accepting/rejecting an invite to an Event
 */
public class EventInvitationFragment extends Fragment {

    private Event event;
    private ApplicantRepository applicantRepository;;

    public EventInvitationFragment() {
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
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

        applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {
                @Override
                public void onDataReceived(List<Applicant> applicants) {
                    entrants.setText(String.format(Locale.getDefault(), "%d waiting to join", applicants.size()));
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), String.format("Error fetching applicants: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }

        });

        User cur = SessionController.getInstance(getContext()).getCurrentUser();

        Button accept = v.findViewById(R.id.invitation_accept_button);

        Button decline = v.findViewById(R.id.invitation_decline_button);


        List<Applicant> cur_applicant = new ArrayList<>();


        applicantRepository.fetchApplicant(cur.getId(), event.getId(), new FirestoreCallbackApplicantReceive() {


            @Override
            public void onDataReceived(Applicant applicant) {
                if (applicant != null) {
                    cur_applicant.add(applicant);
                    if (applicant.getStatus() != ApplicantStatus.INVITED) {
                        accept.setVisibility(GONE);
                        decline.setVisibility(GONE);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), String.format("Error fetching applicant: %s", e.toString()), Toast.LENGTH_SHORT).show();
            }



        });




        accept.setOnClickListener(view -> {

            cur_applicant.get(0).setStatus(ApplicantStatus.ACCEPTED);

            applicantRepository.updateApplicant(cur_applicant.get(0), new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(EventInvitationFragment.this).navigateUp();
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(getContext(), String.format("Error accepting invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                    }
            });

        });


        decline.setOnClickListener(view -> {

            cur_applicant.get(0).setStatus(ApplicantStatus.DECLINED);

            applicantRepository.updateApplicant(cur_applicant.get(0), new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EventInvitationFragment.this).navigateUp();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error declining invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }
            });

        });


        return v;
    }
}