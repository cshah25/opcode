package com.example.opcodeapp.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fragment displaying Event Details to the Entrant.
 * Fulfills US 01.05.04 (Waitlist Count) and US 01.05.05 (Lottery Guidelines).
 */
public class EventDetailsFragment extends Fragment {

    private LinearLayout waitlistSection;
    private LinearLayout invitationSection;
    private LinearLayout entrantSection;

    private MaterialButton joinWaitlistButton;
    private MaterialButton leaveDrawButton;
    private MaterialButton blockNotificationsButton;
    private MaterialButton commentButton;
    private MaterialButton qrCodeButton;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;
    private ImageButton lotteryInfoButton;

    private TextView name;
    private TextView dateLocation;
    private TextView organizer;
    private TextView registration;
    private TextView description;
    private TextView waitlistCount;

    private Event event;
    private User user;
    private Applicant applicant;

    private ApplicantRepository applicantRepository;
    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        userRepository = new UserRepository(FirebaseFirestore.getInstance());

        Bundle args = getArguments();
        if (args == null) {
            navController.navigate(R.id.eventListFragment);
            return;
        }

        event = args.getParcelable("event", Event.class);
        applicant = (args.containsKey("applicant")) ? args.getParcelable("applicant", Applicant.class) : null;
        user = SessionController.getInstance(getContext()).getCurrentUser();

        // Safety check to prevent crashes
        if (event == null) {
            Log.e("EventDetails", "Event data failed to load.");
            navController.navigate(R.id.eventListFragment);
            return;
        }

        // Bind UI Elements to the view
        bindViews(view);

        // Set event details
        name.setText(event.getName());
        description.setText(event.getDescription());
        dateLocation.setText(event.getLocation() + "•" + event.getFormattedDates());
        registration.setText(event.getFormattedRegistration());
        description.setText(event.getDescription());
        userRepository.fetchUser(event.getOrganizerId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                organizer.setText(EventDetailsFragment.this.user.getName());
            }

            @Override
            public void onError(Exception e) {

            }
        });


        // US 01.05.04: Show Waitlist Count
        updateWaitlistCount();


        // US 01.05.05: Show Lottery Criteria
        lotteryInfoButton.setOnClickListener(v -> showLotteryCriteriaDialog());
        joinWaitlistButton.setOnClickListener(v -> joinEventWaitlist());

        // Opens up the QR Code view
        qrCodeButton.setOnClickListener(v ->
                QrCodeViewerFragment.newInstance(event.getId())
                        .show(getParentFragmentManager(), "qr_code_view"));

        leaveDrawButton.setOnClickListener(v -> leaveDraw());
        acceptButton.setOnClickListener(v -> acceptInvitation());
        declineButton.setOnClickListener(v -> declineInvitation());

        updateSections();
        /* commentButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            navController.navigate(R.id.commentsFragment, bundle);
        }); */
    }

    /**
     * Binds all child elements to the view
     *
     * @param view The current view of the fragment
     */
    private void bindViews(View view) {
        waitlistSection = view.findViewById(R.id.waitlist_section);
        invitationSection = view.findViewById(R.id.invited_section);
        entrantSection = view.findViewById(R.id.entrant_section);

        joinWaitlistButton = view.findViewById(R.id.btn_join_waitlist);
        leaveDrawButton = view.findViewById(R.id.btn_leave_draw);
        blockNotificationsButton = view.findViewById(R.id.btn_block_notifications);
        qrCodeButton = view.findViewById(R.id.btn_qr_code);
        acceptButton = view.findViewById(R.id.btn_accept_invitation);
        declineButton = view.findViewById(R.id.btn_decline_invitation);
        lotteryInfoButton = view.findViewById(R.id.btn_lottery_info);
//        commentButton = view.findViewById(R.id.comment_button);

        name = view.findViewById(R.id.tv_event_name);
        dateLocation = view.findViewById(R.id.tv_event_date_location);
        organizer = view.findViewById(R.id.tv_event_organizer);
        registration = view.findViewById(R.id.tv_event_registration_dates);
        description = view.findViewById(R.id.tv_event_description);
        waitlistCount = view.findViewById(R.id.tv_waitlist_count);
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

    private void updateWaitlistCount() {
        applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                if (applicants != null) {
                    int count = applicants.size();
                    int limit = event.getWaitlistLimit();
                    String out = (limit > 0) ? String.format("%d/%d", count, limit) : String.valueOf(count);
                    waitlistCount.setText(out + " people on waitlist");
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * On-click handler for the {@link #joinWaitlistButton}. Creates a new applicant
     * and pushes the change to Firestore and updates the waitlist count and show
     * the {@link #waitlistSection}
     * visible while hiding the unrelated elements
     */
    private void joinEventWaitlist() {
        this.applicant = Applicant.builder()
                .eventId(event.getId())
                .userId(user.getId())
                .name(user.getName())
                .status(ApplicantStatus.NOT_DRAWN)
                .joinedAt(LocalDateTime.now())
                .build();

        applicantRepository.addApplicant(applicant, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                Toast.makeText(requireContext(), "Successfully joined waitlist!", Toast.LENGTH_SHORT).show();
                updateSections();
                updateWaitlistCount();
            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to join waitlist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * On-click handler for the {@link #acceptButton}. Updates the applicant's status to accepted
     * and pushes the change to Firestore and update the UI to make the {@link #entrantSection}
     * visible while hiding the unrelated elements
     */
    private void acceptInvitation() {
        if (applicant != null) {
            applicant.setStatus(ApplicantStatus.ACCEPTED);
            applicantRepository.updateApplicant(applicant, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    applicant.setDirty(false);
                    updateSections();
                    Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error accepting invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void declineInvitation() {
        if (applicant != null) {
            applicant.setStatus(ApplicantStatus.DECLINED);
            applicantRepository.updateApplicant(applicant, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                    updateSections();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error declining invitation: %s", e.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void leaveDraw() {
        if (applicant != null) {
            applicantRepository.deleteApplicant(applicant.getId(), new FirestoreCallbackSend() {

                /**
                 * Go back to the event list when the applicant is deleted
                 */
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Leaving the draw", Toast.LENGTH_SHORT).show();
                    updateSections();
                    updateWaitlistCount();
                }

                /**
                 * Notify the user the leave draw handler failed
                 * @param e The exception that occurred
                 */
                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), "Error removing applicant", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateSections() {
        if (applicant == null) {
            waitlistSection.setVisibility(View.VISIBLE);
            joinWaitlistButton.setEnabled(true);
            entrantSection.setVisibility(View.GONE);
            invitationSection.setVisibility(View.GONE);
            return;
        }

        switch (applicant.getStatus()) {
            case INVITED:
                invitationSection.setVisibility(View.VISIBLE);
                entrantSection.setVisibility(View.GONE);
                waitlistSection.setVisibility(View.GONE);
                break;
            case ACCEPTED:
                invitationSection.setVisibility(View.GONE);
                entrantSection.setVisibility(View.VISIBLE);
                waitlistSection.setVisibility(View.GONE);
                break;
            case NOT_DRAWN:
                entrantSection.setVisibility(View.VISIBLE);
                joinWaitlistButton.setEnabled(false);
                invitationSection.setVisibility(View.GONE);
                waitlistSection.setVisibility(View.GONE);
                break;
            default:
                entrantSection.setVisibility(View.GONE);
                joinWaitlistButton.setEnabled(false);
                invitationSection.setVisibility(View.GONE);
                waitlistSection.setVisibility(View.GONE);
                break;
        }
    }
}
