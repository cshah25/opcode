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

    private LinearLayout invitationSection;

    private ImageButton joinLeaveWaitlistButton;
    private ImageButton toggleNotificationsButton;
    private ImageButton adminDeleteButton;
    private ImageButton commentButton;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;
    private ImageButton lotteryInfoButton;

    private TextView name;
    private TextView location;
    private TextView date;
    private TextView organizer;
    private TextView registration;
    private TextView description;
    private TextView waitlistCount;

    private Event event;
    private User user;
    private Applicant applicant;

    private ApplicantRepository applicantRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());

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
        location.setText(event.getLocation());
        date.setText(event.getFormattedDates());
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
        joinLeaveWaitlistButton.setOnClickListener(v -> joinLeaveHandler());
        toggleNotificationsButton.setOnClickListener(v -> toggleNotifications());
        acceptButton.setOnClickListener(v -> handleInvitationResponse(ApplicantStatus.ACCEPTED));
        declineButton.setOnClickListener(v -> handleInvitationResponse(ApplicantStatus.DECLINED));
        adminDeleteButton.setOnClickListener(v -> adminDeleteEvent());
        updateUI();
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
        invitationSection = view.findViewById(R.id.invited_section);

        joinLeaveWaitlistButton = view.findViewById(R.id.btn_join_leave_waitlist);
        toggleNotificationsButton = view.findViewById(R.id.btn_toggle_notifications);
        adminDeleteButton = view.findViewById(R.id.btn_admin_delete);
        acceptButton = view.findViewById(R.id.btn_accept_invitation);
        declineButton = view.findViewById(R.id.btn_decline_invitation);
        lotteryInfoButton = view.findViewById(R.id.btn_lottery_info);
//        commentButton = view.findViewById(R.id.comment_button);

        name = view.findViewById(R.id.tv_event_name);
        date = view.findViewById(R.id.tv_event_date);
        location = view.findViewById(R.id.tv_event_location);
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
     * On-click handler for the {@link #joinLeaveWaitlistButton}. If there is no existing applicant,
     * a new applicant is created and published to Firestore, otherwise the applicant is deleted.
     * In both cases, the waitlist count and the button's content description and icon is also updated
     */
    private void joinLeaveHandler() {
        if (applicant == null) {
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
                    joinLeaveWaitlistButton.setContentDescription("Leave Waitlist");
                    joinLeaveWaitlistButton.setImageResource(R.drawable.ic_leave_waitlist);
                    updateUI();
                    updateWaitlistCount();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to join waitlist.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            applicantRepository.deleteApplicant(applicant.getId(), new FirestoreCallbackSend() {

                /**
                 * Go back to the event list when the applicant is deleted
                 */
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Leaving the draw", Toast.LENGTH_SHORT).show();
                    joinLeaveWaitlistButton.setContentDescription("Join Waitlist");
                    joinLeaveWaitlistButton.setImageResource(R.drawable.ic_join_waitlist);
                    applicant = null;
                    updateUI();
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

    // TODO: Merge from notification commits
    private void toggleNotifications() {

    }

    // TODO: Merge from admin commits
    private void adminDeleteEvent() {

    }

    /**
     * On-click handler for the accept and decline buttons. Updates the applicant's status
     * and pushes the change to Firestore while also updating the UI to hide the
     * accept/decline buttons
     */
    private void handleInvitationResponse(ApplicantStatus status) {
        if (applicant != null) {
            applicant.setStatus(status);
            applicantRepository.updateApplicant(applicant, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    applicant.setDirty(false);
                    Toast.makeText(getContext(), "Invitation " + status.name().toLowerCase(), Toast.LENGTH_SHORT).show();
                    updateUI();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), String.format("Error when setting status to %s\nError: %s", status.name(), e.toString()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Refreshes the fragment to hide/show the {@link #adminDeleteButton} if the user's admin status
     * changes. Furthermore, the visibility of the {@link #joinLeaveWaitlistButton}, {@link #toggleNotificationsButton},
     * {@link #acceptButton} and {@link #declineButton}
     */
    private void updateUI() {
        adminDeleteButton.setVisibility((user.isAdmin()) ? View.VISIBLE : View.GONE);

        if (applicant == null) {
            joinLeaveWaitlistButton.setVisibility(View.VISIBLE);
            invitationSection.setVisibility(View.GONE);
            toggleNotificationsButton.setVisibility(View.GONE);
            return;
        }

        switch (applicant.getStatus()) {
            case NOT_DRAWN:
                invitationSection.setVisibility(View.GONE);
                joinLeaveWaitlistButton.setVisibility(View.VISIBLE);
                break;
            case INVITED:
                invitationSection.setVisibility(View.VISIBLE);
                joinLeaveWaitlistButton.setVisibility(View.GONE);
                break;
            default:
                invitationSection.setVisibility(View.GONE);
                joinLeaveWaitlistButton.setVisibility(View.GONE);
                break;
        }
        toggleNotificationsButton.setVisibility(View.VISIBLE);
    }
}
