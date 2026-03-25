package com.example.opcodeapp.view;

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

import com.example.opcodeapp.adapter.UserArrayAdapter;
import com.example.opcodeapp.callback.DBManager;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.LotterySystem;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the event waitlist and running the lottery draw.
 * Fulfills responsibilities from the LotterySystem CRC card.
 */
public class WaitListFragment extends Fragment {

    private Event currentEvent;
    private User currentUser;
    private ApplicantRepository applicantRepository;
    private LotterySystem lotterySystem;

    private ListView waitlistListView;
    private ArrayAdapter<Applicant> adapter;
    private ArrayList<Applicant> applicantDataList;

    private EditText numToDrawInput;
    private View lotteryControls;

    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.waitlist_screen, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Managers and Data
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        lotterySystem = new LotterySystem();


        currentUser = SessionController.getInstance(getContext()).getCurrentUser();

        if (getArguments() != null) {
            currentEvent = (Event) getArguments().getParcelable("event");

        }

        // Setup UI References
        waitlistListView = view.findViewById(R.id.waitlist_list_view);
        numToDrawInput = view.findViewById(R.id.num_to_draw_input);
        lotteryControls = view.findViewById(R.id.lottery_controls);
        Button drawButton = view.findViewById(R.id.btn_draw_lottery);
        TextView header = view.findViewById(R.id.event_header);

        header.setText(currentEvent.getName() + " Waitlist");

        // Initialize List and Adapter
        applicantDataList = new ArrayList<>();

        applicantRepository.fetchApplicantsByStatus(currentEvent, ApplicantStatus.NOT_DRAWN, new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                applicantDataList = new ArrayList<>(applicants);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Nobody in waiting list!", Toast.LENGTH_SHORT).show();
            }

        });




        adapter = new UserArrayAdapter(getContext(), applicantDataList);
        waitlistListView.setAdapter(adapter);

        // Responsibility:  only Organizers can see lottery controls
        if (!currentUser.getId().equals(currentEvent.getOrganizer().getId())) {
            lotteryControls.setVisibility(View.GONE);
        }

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

        int numToDraw = Integer.parseInt(input);

        // Responsibility: randomly select entrants

        List<Applicant> winners = lotterySystem.drawEntrants(currentEvent, numToDraw);

        if (winners==null || winners.isEmpty()) {
            Toast.makeText(requireContext(), "Waitlist is empty or no winners selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Responsibility: notify entrants
        processWinner(winners);

        Toast.makeText(requireContext(), "Selected " + winners.size() + " winners", Toast.LENGTH_LONG).show();    }

    /**
     * Passes the winning users to the Event instance to update their invited status.
     */
    private void processWinner(List<Applicant> winners) {
        for (Applicant winner: winners) {
            winner.setStatus(ApplicantStatus.INVITED);
            applicantRepository.updateApplicant(winner, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    Log.d("Lottery", "Applicant Invited!");
                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.d("Lottery", "Failed to invite applicant!");
                }


            });







        }
        Log.d("Lottery", "Winners are invited!");

    }
}
