package com.example.opcodeapp.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.ApplicantArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.NotificationRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment for displaying the event waitlist and running the lottery draw.
 * Fulfills responsibilities from the LotterySystem CRC card.
 */
public class WaitListFragment extends Fragment {

    private Event event;
    private ApplicantRepository applicantRepository;
    private NotificationRepository notificationRepository;
    private ArrayAdapter<Applicant> adapter;

    private EditText numToDrawInput;
    private final List<Applicant> waitlist = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.waitlist_screen, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("MissingBundle", "No arguments set before navigating to this fragment");
            NavHostFragment.findNavController(WaitListFragment.this).navigateUp();
            return;
        }

        event = args.getParcelable("event", Event.class);
        if (event == null) {
            Log.e("MissingBundleArgs", "Missing Event argument in bundle");
            NavHostFragment.findNavController(WaitListFragment.this).navigateUp();
            return;
        }

        // Initialize Repository and Data
        User user = SessionController.getInstance(requireContext()).getCurrentUser();
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        notificationRepository = new NotificationRepository(FirebaseFirestore.getInstance());

        // Setup UI References
        ListView waitlistListView = view.findViewById(R.id.waitlist_list_view);
        numToDrawInput = view.findViewById(R.id.num_to_draw_input);
        View lotteryControls = view.findViewById(R.id.lottery_controls);
        Button drawButton = view.findViewById(R.id.btn_draw_lottery);
        TextView header = view.findViewById(R.id.event_header);

        header.setText(event.getName() + " Waitlist");

        // Initialize and attach adapter
        applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicant) {
                for (Applicant a : applicant) {
                    if (a.getStatus() == ApplicantStatus.NOT_DRAWN) {
                        waitlist.add(a);
                    }
                }
                adapter = new ApplicantArrayAdapter(getContext(), waitlist);
                waitlistListView.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Log.e("FetchApplicantError", "An error occurred: " + e.getMessage());
            }
        });

        // Responsibility:  only Organizers can see lottery controls
        if (!user.getId().equals(event.getOrganizerId()))
            lotteryControls.setVisibility(View.GONE);

        // Setup Lottery Draw Listener
        drawButton.setOnClickListener(v -> runLotteryDraw());
    }

    /**
     * Executes the random selection logic defined in the LotterySystem.
     */
    private void runLotteryDraw() {
        String input = numToDrawInput.getText().toString();
        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a number to draw", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int numToDraw = Integer.parseInt(input);
            List<Applicant> result = new ArrayList<>();

            ApplicantRepository repository = new ApplicantRepository(FirebaseFirestore.getInstance());
            repository.fetchApplicantsByStatus(event, ApplicantStatus.NOT_DRAWN,
                    new FirestoreCallbackApplicantsReceive() {
                        @Override
                        public void onDataReceived(List<Applicant> applicant) {
                            result.addAll(applicant);
                            int drawSize = Math.min(numToDraw, result.size());

                            // Responsibility: randomly assign entrants
                            Collections.shuffle(result);

                            List<Applicant> winners = new ArrayList<>(result.subList(0, drawSize));
                            if (winners.isEmpty()) {
                                Toast.makeText(requireContext(), "Waitlist is empty or no winners selected", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            List<Applicant> losers = new ArrayList<>(result.subList(drawSize, result.size()));

                            for (Applicant winner : winners) {
                                waitlist.removeIf(a -> a.getId().equals(winner.getId()));
                            }

                            adapter.notifyDataSetChanged();

                            // Responsibility: notify entrants
                            processWinner(winners);
                            processLosers(losers);
                            Toast.makeText(requireContext(), "Selected " + winners.size() + " winners", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    }
            );


            // Responsibility: randomly select entrants

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Could not parse number", Toast.LENGTH_SHORT).show();
        }
    }

    private void processLosers(List<Applicant> losers) {
        losers.forEach(applicant -> {
            notificationRepository.addNotification(new Notification(applicant.getUserId(), String.format("You missed the lottery to %s. :(", event.getName()), event.getId(), "event_detail"), new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    Log.i("Lottery", "notification created for loser");
                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.e("Lottery", "Could not add notification for loser", e);
                }
            });
        });
    }

    /**
     * Passes the winning users to the Event instance to update their invited status.
     */
    private void processWinner(List<Applicant> winners) {
        //probably needed
        winners.forEach(applicant -> {
            applicant.setStatus(ApplicantStatus.INVITED);
            applicantRepository.updateApplicant(applicant, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    applicant.setDirty(false);
                    Log.d("Lottery", "Applicant updated with invited users.");
                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.e("Lottery", "Failed to update event", e);
                }
            });
            // create notification inviting user
            notificationRepository.addNotification(new Notification(applicant.getUserId(), String.format("You're invited to %s!", event.getName()), event.getId(), "event_detail"), new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    Log.i("NotificationWaitlist", "notification created for winner");
                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.e("NotificationWaitlist", "Could not add notification for winner", e);
                }
            });
        });
    }
}
