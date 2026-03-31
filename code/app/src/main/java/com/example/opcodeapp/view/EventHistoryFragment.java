package com.example.opcodeapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.DBManager;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fulfills US 01.02.03: Entrant can view a history of events they registered for.
 */
public class EventHistoryFragment extends Fragment {

    private User currentUser;
    private DBManager dbManager;
    private ListView historyListView;
    private ArrayList<String> displayList;
    private ArrayAdapter<String> adapter;
    private ApplicantRepository applicantRepository;
    private EventRepository eventRepository;

    public EventHistoryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_history, container, false);
    }

    /**
     * Initializes the views, repositories, and triggers the fetching of the user's event history.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories using the Firebase instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        applicantRepository = new ApplicantRepository(db);
        eventRepository = new EventRepository(db);

        if (getArguments() != null) {
            currentUser = (User) getArguments().getParcelable("CURRENT_USER");
        }

        historyListView = view.findViewById(R.id.list_event_history);
        displayList = new ArrayList<>();

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, displayList);
        historyListView.setAdapter(adapter);

        if (currentUser != null) {
            loadUserHistory();
        } else {
            Toast.makeText(requireContext(), "User not found:", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches the applicant records for the current user from the repository.
     * If records are found, loads the corresponding event details.
     */
    private void loadUserHistory() {
        applicantRepository.fetchApplicants(
                query -> query.whereEqualTo("userId", currentUser.getId()),
                new FirestoreCallbackApplicantsReceive() {
                    @Override
                    public void onDataReceived(List<Applicant> applicants) {
                        displayList.clear();

                        if (applicants.isEmpty()) {
                            Toast.makeText(requireContext(), "No event history found.", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        for (Applicant applicant : applicants) {
                            fetchEventDetailsForApplicant(applicant);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("EventHistory", "Error fetching applicant history", e);
                        Toast.makeText(requireContext(), "Failed to load history.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Fetches the specific event details associated with a given applicant record
     * and adds the formatted result to the list view display.
     *
     * @param applicant The applicant record containing the event ID and status.
     */
    private void fetchEventDetailsForApplicant(Applicant applicant) {
        eventRepository.fetchEvent(applicant.getEventId(), new FirestoreCallbackEventReceive() {
            @Override
            public void onDataReceived(Event event) {
                String statusString = applicant.getStatus().name();

                String displayString = event.getName() + " - Status: " + statusString;
                displayList.add(displayString);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("EventHistory", "Error fetching event details for ID: " + applicant.getEventId(), e);
                displayList.add(applicant.getStatus().name());
                adapter.notifyDataSetChanged();
            }
        });
    }
}
