package com.example.opcodeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for displaying the event waitlist and running the lottery draw.
 * Fulfills responsibilities from the LotterySystem CRC card.
 */
public class WaitListActivity extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waitlist_screen);

        // Initialize Managers and Data
        dbManager = new DBManager();
        lotterySystem = new LotterySystem();

        currentEvent = (Event) getIntent().getSerializableExtra("EVENT");
        currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");

        // Setup UI References
        waitlistListView = findViewById(R.id.waitlist_list_view);
        numToDrawInput = findViewById(R.id.num_to_draw_input);
        lotteryControls = findViewById(R.id.lottery_controls);
        Button drawButton = findViewById(R.id.btn_draw_lottery);
        TextView header = findViewById(R.id.event_header);

        header.setText(currentEvent.getName() + " Waitlist");

        // Initialize List and Adapter
        User[] applicants = currentEvent.getApplicants();
        if (applicants == null)
            applicantDataList = new ArrayList<>();
        else
            applicantDataList = new ArrayList<>(Arrays.asList(applicants));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, applicantDataList);
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
            Toast.makeText(this, "Enter a number to draw", Toast.LENGTH_SHORT).show();
            return;
        }

        int numToDraw = Integer.parseInt(input);

        // Responsibility: randomly select entrants
        List<User> winners = lotterySystem.drawEntrants(currentEvent, numToDraw);

        if (winners.isEmpty()) {
            Toast.makeText(this, "Waitlist is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Responsibility: notify entrants
        for (User winner : winners) {
            processWinner(winner);
        }

        Toast.makeText(this, "Selected " + winners.size() + " winners", Toast.LENGTH_LONG).show();
    }

    /**
     * Updates winner status and sends notification via DBManager.
     */
    private void processWinner(User winner) {
        // winner.setStatus("Selected"); // something like this

        // Send to Firestore
        dbManager.updateUser(winner, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess() {
                Log.d("Lottery", "User " + winner.getId() + " updated to Selected.");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.e("Lottery", "Failed to update user", e);
            }
        });

        // notification logic here later using DBManager probably
    }
}
