package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsFragment extends Fragment {

    private Event event;

    public EventDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            event = getArguments().getParcelable("event");
        }

        if (event == null) {
            NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();
            return;
        }

        User currentUser = SessionController.getInstance(requireContext()).getCurrentUser();
        ImageButton backButton = view.findViewById(R.id.event_back_button);

        TextView nameText = view.findViewById(R.id.event_name_text);
        TextView dateText = view.findViewById(R.id.event_date_text);
        TextView locationText = view.findViewById(R.id.event_location_text);
        TextView descriptionText = view.findViewById(R.id.event_description_text);

        Button leaveDrawButton = view.findViewById(R.id.leave_draw_button);
        Button qrCodeButton = view.findViewById(R.id.qr_code_button);

        nameText.setText(event.getName());
        dateText.setText(event.getStartDate() + " to " + event.getEndDate());
        locationText.setText(event.getLocation());
        descriptionText.setText(event.getDescription());


        view.findViewById(R.id.event_profile_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                NavHostFragment.findNavController(EventDetailsFragment.this).navigate(R.id.ProfileFragment);
            }

        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();
            }
        });

        leaveDrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    return;
                }

                event.removeUser(currentUser);
                DBManager db = new DBManager(FirebaseFirestore.getInstance());
                db.updateEvent(event, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess() {
                        NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                    }
                });
            }
        });

        qrCodeButton.setOnClickListener(v  -> {
            QrCodeViewerFragment.newInstance(event.getId())
                    .show(getParentFragmentManager(), "qr_code_view");
        });
    }
}