package com.example.opcodeapp.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment displaying Event Details to the Entrant.
 */
public class EventDetailsFragment extends Fragment {

    private LinearLayout invitationSection;

    private ImageButton joinLeaveWaitlistButton;
    private ImageButton toggleNotificationsButton;
    private ImageButton adminDeleteButton;
    private LinearLayout commentButton;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;
    private Button removeOrganizerButton;
    private ImageButton lotteryInfoButton;

    private TextView name;
    private TextView location;
    private TextView date;
    private TextView organizer;
    private TextView registration;
    private TextView description;
    private TextView waitlistCount;
    private ImageView eventPoster;

    private Event event;
    private User currUser;
    private Applicant applicant;

    private ApplicantRepository applicantRepository;
    private EventRepository eventRepository;

    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    /**
     * Inflates the event details screen layout.
     *
     * @param inflater           the layout inflater for this fragment
     * @param container          the parent view that will host the fragment
     * @param savedInstanceState the previously saved state, if any
     * @return the inflated event-details screen
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Loads the selected event, binds the controls, and wires the waitlist actions.
     *
     * @param view               the fragment root view
     * @param savedInstanceState the previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        eventRepository = new EventRepository(FirebaseFirestore.getInstance());
        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());

        Bundle args = getArguments();
        if (args == null) {
            navController.navigate(R.id.eventListFragment);
            return;
        }

        event = args.getParcelable("event", Event.class);
        currUser = SessionController.getInstance(getContext()).getCurrentUser();
        if (currUser == null) {
            Toast.makeText(requireContext(), "Could not retrieve current user", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.setupFragment);
            return;
        }

        // Safety check to prevent crashes
        if (event == null) {
            Log.e("EventDetails", "Event data failed to load.");
            navController.navigate(R.id.eventListFragment);
            return;
        }

        // Bind UI Elements to the view
        bindViews(view);

        applicantRepository.fetchApplicant(currUser.getId(), event.getId(), new FirestoreCallbackApplicantReceive() {
            @Override
            public void onDataReceived(@Nullable Applicant a) {
                applicant = a;
                updateUI();
            }

            @Override
            public void onError(Exception e) {
                Log.e("EventDetails", "Could not retrieve applicant");
                updateUI();
            }
        });

        // Set event details
        name.setText(event.getName());
        description.setText(event.getDescription());
        location.setText(event.getLocation());
        date.setText(event.getFormattedDates());
        registration.setText("Registration: " + event.getFormattedRegistration());
        description.setText(event.getDescription());
        userRepository.fetchUser(event.getOrganizerId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                organizer.setText(user.getName());
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

        //User Story 03.01.01
        adminDeleteButton.setOnClickListener(v -> adminDeleteEvent(this));
        removeOrganizerButton.setOnClickListener(v -> adminDeleteOrganizer());
        loadPosterImage();

        commentButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            navController.navigate(R.id.commentsFragment, bundle);
        });
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
        commentButton = view.findViewById(R.id.btn_comment_row);
        removeOrganizerButton = view.findViewById(R.id.btn_remove_organizer);


        name = view.findViewById(R.id.tv_event_name);
        date = view.findViewById(R.id.tv_event_date);
        location = view.findViewById(R.id.tv_event_location);
        organizer = view.findViewById(R.id.tv_event_organizer);
        registration = view.findViewById(R.id.tv_event_registration_dates);
        description = view.findViewById(R.id.tv_event_description);
        waitlistCount = view.findViewById(R.id.tv_waitlist_count);
        eventPoster = view.findViewById(R.id.iv_event_poster);
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

    /**
     * Refreshes the waitlist count displayed for the current event and pushes any uncommitted
     * updates to Firestore
     */
    @SuppressLint("DefaultLocale")
    private void updateWaitlistCount() {
        int count = event.getWaitlistCount();
        int limit = event.getWaitlistLimit();
        String out = (limit > 0) ? String.format("%d/%d", count, limit) : String.valueOf(count);
        waitlistCount.setText(out + " people on waitlist");

        if (event.isDirty()) {
            eventRepository.updateEvent(event, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    event.setDirty(false);
                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.e("EventDetailsFragment", "Error updating event");
                }
            });
        }
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
                    .userId(currUser.getId())
                    .name(currUser.getName())
                    .status(ApplicantStatus.NOT_DRAWN)
                    .joinedAt(LocalDateTime.now())
                    .build();

            applicantRepository.addApplicant(applicant, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    Toast.makeText(requireContext(), "Successfully joined waitlist!", Toast.LENGTH_SHORT).show();
                    joinLeaveWaitlistButton.setContentDescription("Leave Waitlist");
                    joinLeaveWaitlistButton.setImageResource(R.drawable.ic_leave_waitlist);
                    event.incrementWaitlistCount();
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
                 * Updates the waitlist count and controls when the applicant is deleted.
                 */
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(getContext(), "Leaving the draw", Toast.LENGTH_SHORT).show();
                    joinLeaveWaitlistButton.setContentDescription("Join Waitlist");
                    joinLeaveWaitlistButton.setImageResource(R.drawable.ic_join_waitlist);
                    applicant = null;
                    event.decrementWaitlistCount();
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
    /**
     * Placeholder for notification opt-in handling on the event-details screen.
     */
    private void toggleNotifications() {

    }

    /**
     * On-click handler for the admin delete button. This will delete the current event and navigate
     * to the event list fragment
     */
    private void adminDeleteEvent(EventDetailsFragment fragment) {

        //code added by Vedant to make sure that all the applicants of the event are also deleted along with the event.
        applicantRepository.deleteApplicantsByEvent(event.getId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("EventDetails", "Deleted applicants as a result of event deletion.");

            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to delete applicants.", Toast.LENGTH_SHORT).show();
            }

        });

        eventRepository.deleteEvent(event.getId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Event removed by Admin.", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(fragment).navigate(R.id.eventListFragment);
            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles decoding the event poster string from Base64 and setting the image view. This is run
     * on a separate thread as to not block the main thread. A placeholder image is used during loading
     */
    private void loadPosterImage() {
        int placeholderImage = R.drawable.image_placeholder;
        eventPoster.setImageResource(placeholderImage);

        String encodedPoster = event.getEncodedImage();
        if (encodedPoster == null || encodedPoster.isEmpty()) {
            return;
        }

        imageExecutor.execute(() -> {
            try {
                byte[] bytes = Base64.decode(encodedPoster, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (bitmap != null) {
                        eventPoster.setImageBitmap(bitmap);
                    } else {
                        eventPoster.setImageResource(placeholderImage);
                    }
                });

            } catch (Exception e) {
                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> eventPoster.setImageResource(placeholderImage));
            }
        });
    }

    /**
     * Deletes the organizer and their related event/application records from the admin view.
     */
    private void adminDeleteOrganizer() {
        UserRepository user_repo = new UserRepository(FirebaseFirestore.getInstance());
        user_repo.deleteUser(event.getOrganizerId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("EventDetails", "Deleted organizer.");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.d("EventDetails", "Failed to delete organizer.");
            }
        });

        EventRepository event_repo = new EventRepository(FirebaseFirestore.getInstance());

        event_repo.deleteEventsByOrganizerId(event.getOrganizerId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("EventDetails", "Deleted events by organizer.");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.d("EventDetails", "Failed to delete events by organizer.");
            }

        });

        ApplicantRepository applicant_repo = new ApplicantRepository(FirebaseFirestore.getInstance());
        applicant_repo.deleteApplicantsByUser(event.getOrganizerId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Log.d("EventDetails", "Deleted applicants by organizer.");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.d("EventDetails", "Failed to delete applicants by organizer.");
            }
        });

        NavHostFragment.findNavController(this).navigate(R.id.eventListFragment);
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
        adminDeleteButton.setVisibility((currUser != null && currUser.isAdmin()) ? View.VISIBLE : View.GONE);
        removeOrganizerButton.setVisibility((currUser != null && currUser.isAdmin() && !Objects.equals(event.getOrganizerId(), currUser.getId())) ? View.VISIBLE : View.GONE);

        if (applicant == null) {
            joinLeaveWaitlistButton.setVisibility(View.VISIBLE);
            invitationSection.setVisibility(View.GONE);
            toggleNotificationsButton.setVisibility(View.GONE);
            commentButton.setVisibility(View.GONE);
            return;
        }

        switch (applicant.getStatus()) {
            case NOT_DRAWN:
                invitationSection.setVisibility(View.GONE);
                joinLeaveWaitlistButton.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                break;
            case INVITED:
                invitationSection.setVisibility(View.VISIBLE);
                joinLeaveWaitlistButton.setVisibility(View.GONE);
                commentButton.setVisibility(View.VISIBLE);
                break;
            default:
                invitationSection.setVisibility(View.GONE);
                joinLeaveWaitlistButton.setVisibility(View.GONE);
                commentButton.setVisibility(View.VISIBLE);
                break;
        }
        toggleNotificationsButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageExecutor.shutdownNow();
    }
}
