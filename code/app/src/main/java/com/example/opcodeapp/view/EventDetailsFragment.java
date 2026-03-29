package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
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

        TextView nameText = view.findViewById(R.id.event_name_text);
        TextView dateText = view.findViewById(R.id.event_date_text);
        TextView locationText = view.findViewById(R.id.event_location_text);
        TextView descriptionText = view.findViewById(R.id.event_description_text);

        Button leaveDrawButton = view.findViewById(R.id.leave_draw_button);
        Button qrCodeButton = view.findViewById(R.id.qr_code_button);

        nameText.setText(event.getName());
        dateText.setText(event.getStart() + " to " + event.getEnd());
        locationText.setText(event.getLocation());
        descriptionText.setText(event.getDescription());


        leaveDrawButton.setOnClickListener(v -> {
            if (currentUser == null) {
                return;
            }

                //event.removeUser(currentUser);
                ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

                applicantRepository.fetchApplicant(currentUser.getId(), event.getId(), new FirestoreCallbackApplicantReceive() {
                    @Override
                    public void onDataReceived(Applicant applicant) {

                        applicantRepository.deleteApplicant(applicant.getId(), new FirestoreCallbackSend() {
                            @Override
                            public void onSendSuccess(Void aVoid) {
                                NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();

                            }

                            @Override
                            public void onSendFailure(Exception e) {
                                Toast.makeText(getContext(), "Error removing applicant", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();
                            }
                        });

                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Error fetching applicant", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(EventDetailsFragment.this).navigateUp();
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