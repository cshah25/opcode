package com.example.opcodeapp;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.DBManager;
import com.example.opcodeapp.FirestoreCallbackSend;
import com.example.opcodeapp.LotterySystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment for displaying the event waitlist and running the lottery draw.
 * Fulfills responsibilities from the LotterySystem CRC card.
 */
public class WaitListFragment extends Fragment {

    private Event currentEvent;
    private User currentUser;
    private DBManager dbManager;
    private LotterySystem lotterySystem;

    private ListView waitlistListView;
    private ArrayAdapter<User> adapter;
    private ArrayList<User> applicantDataList;

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
        dbManager = new DBManager();
        lotterySystem = new LotterySystem();

        if (getArguments() != null) {
            currentEvent = (Event) getArguments().getSerializable("EVENT");
            currentUser = (User) getArguments().getSerializable("CURRENT_USER");
        }

        // Setup UI References
        waitlistListView = view.findViewById(R.id.waitlist_list_view);
        numToDrawInput = view.findViewById(R.id.num_to_draw_input);
        lotteryControls = view.findViewById(R.id.lottery_controls);
        Button drawButton = view.findViewById(R.id.btn_draw_lottery);
        TextView header = view.findViewById(R.id.event_header);

        header.setText(currentEvent.getName() + " Waitlist");

        // Initialize List and Adapter
        User[] applicants = currentEvent.getApplicants();
        if (applicants == null)
            applicantDataList = new ArrayList<>();
        else
            applicantDataList = new ArrayList<>(Arrays.asList(applicants));

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, applicantDataList);
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
        List<User> winners = lotterySystem.drawEntrants(currentEvent, numToDraw);

        if (winners==null || winners.isEmpty()) {
            Toast.makeText(requireContext(), "Waitlist is empty or no winners selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Responsibility: notify entrants
        processWinners(winners);

        Toast.makeText(requireContext(), "Selected " + winners.size() + " winners", Toast.LENGTH_LONG).show();    }

    /**
     * Passes the winning users to the Event instance to update their invited status.
     */
    private void processWinner(List<User> winners) {
        currentEvent.setInvited(winners);
        Log.d("Lottery", "Winners passed to Event.setInvited()");

        //probably needed
        dbManager.updateEvent(currentEvent, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess() { Log.d("Lottery", "Event updated with invited users."); }
            @Override
            public void onSendFailure(Exception e) { Log.e("Lottery", "Failed to update event", e); }
        });
    }
}
