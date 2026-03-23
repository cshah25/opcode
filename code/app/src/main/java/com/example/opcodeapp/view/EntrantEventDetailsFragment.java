package com.example.opcodeapp.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.db.FirestoreCallbackSend;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fragment displaying Event Details to the Entrant.
 * Fulfills US 01.05.04 (Waitlist Count) and US 01.05.05 (Lottery Guidelines).
 */
public class EntrantEventDetailsFragment extends Fragment {

    private DBManager dbManager;

    private Event currentEvent;
    private User currentUser;

    public EntrantEventDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout we just created
        return inflater.inflate(R.layout.fragment_entrant_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbManager = new DBManager(FirebaseFirestore.getInstance());


        // 1. Extract the Event and User passed from the previous screen
        if (getArguments() != null) {
            currentEvent = (Event) getArguments().getParcelable("event");

        }

        currentUser = SessionController.getInstance(getContext()).getCurrentUser();


        // Safety check to prevent crashes
        if (currentEvent == null) {
            Log.e("EventDetails", "Event data failed to load.");
            return;
        }

        // Bind UI Elements to the view
        TextView tvEventName = view.findViewById(R.id.tv_event_name);
        TextView tvEventDateLoc = view.findViewById(R.id.tv_event_date_location);
        TextView tvEventDesc = view.findViewById(R.id.tv_event_description);
        TextView tvWaitlistCount = view.findViewById(R.id.tv_waitlist_count);
        ImageButton btnLotteryInfo = view.findViewById(R.id.btn_lottery_info);
        Button btnJoinWaitlist = view.findViewById(R.id.btn_join_waitlist);

        tvEventName.setText(currentEvent.getName());
        tvEventDesc.setText(currentEvent.getDescription());
        tvEventDateLoc.setText(currentEvent.getLocation());

        // US 01.05.04: Show Waitlist Count


        dbManager.fetchEventApplicants(currentEvent, new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                if (applicants != null) {
                    int count = applicants.size();
                    tvWaitlistCount.setText(count + " people on waitlist");
                }

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
            }

        });

        // US 01.05.05: Show Lottery Criteria
        btnLotteryInfo.setOnClickListener(v -> showLotteryCriteriaDialog());

        btnJoinWaitlist.setOnClickListener(v -> joinEventWaitlist(view));
    }

    /**
     * Builds and displays an AlertDialog explaining the rules of the lottery.
     */
    private void showLotteryCriteriaDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lottery Selection Guidelines")
                .setMessage("This event uses a random lottery system for ticket allocation.\n\n" +
                        "• Selection is completely random.\n" +
                        "• The draw will occur prior to the event start date.\n" +
                        "• If selected, you will have a limited time to accept or decline the invitation.\n" +
                        "• Unaccepted invitations will be redrawn to the next person on the waitlist.")
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void joinEventWaitlist(View view) {
        // Add user to local event object




        dbManager.fetchEventApplicants(currentEvent, new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                if (applicants != null) {

                    for (Applicant u : applicants) {
                        if (u.getUserId().equals(currentUser.getId())) {
                            Toast.makeText(requireContext(), "You are already on the waitlist!", Toast.LENGTH_SHORT).show();
                            navigateNext(view);
                            return;
                        }
                    }


                    //new code added by Vedant to check if a waitlist is full.

                    if (currentEvent.getWaitlistLimit() != -1 && applicants.size() >= currentEvent.getWaitlistLimit()) {
                        Toast.makeText(requireContext(), "Waitlist is full!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

                Applicant.Builder b = Applicant.builder("")
                        .eventId(currentEvent.getId())
                        .userId(currentUser.getId())
                        .name(currentUser.getName())
                        .status(ApplicantStatus.NOT_DRAWN)
                        .joinedAt(LocalDateTime.now());

                Applicant currentApplicant = b.build();

                dbManager.addApplicant(currentApplicant, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void aVoid) {
                        Toast.makeText(requireContext(), "Successfully joined waitlist!", Toast.LENGTH_SHORT).show();
                        navigateNext(view);
                    }
                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Failed to join waitlist.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void navigateNext(View view) {

        Navigation.findNavController(view).navigate(R.id.eventsFragment);
    }
}
