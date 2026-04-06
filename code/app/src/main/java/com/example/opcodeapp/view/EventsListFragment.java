package com.example.opcodeapp.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.util.EventFilterUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the event list and lets entrants search and filter the available events.
 */
public class EventsListFragment extends Fragment {

    private Button searchButton;
    private EditText searchInput;
    private ListView eventListView;
    private CheckBox availableOnlyFilter;
    private CheckBox capacityOnlyFilter;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> shownEvents = new ArrayList<>();
    private List<String> shownNames = new ArrayList<>();
    private Map<String, Integer> applicantCounts = new HashMap<>();

    private ArrayAdapter<String> adapter;
    private ApplicantRepository applicantRepository;
    private User user;

    /**
     * Inflates the event-list screen layout.
     *
     * @param inflater           the layout inflater for this fragment
     * @param container          the parent view that will host the fragment
     * @param savedInstanceState the previously saved state, if any
     * @return the inflated event-list screen
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

    /**
     * Binds the event-list controls, loads the event data, and wires up search/filter listeners.
     *
     * @param view               the fragment root view
     * @param savedInstanceState the previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if the current user is non-null
        user = SessionController.getInstance(requireContext()).getCurrentUser();
        if (user == null) {
            NavHostFragment.findNavController(this).navigate(R.id.setupFragment);
            Log.e("EventListFragment", "Could not retrieve the current user");
            return;
        }

        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

        // Bind views
        searchButton = view.findViewById(R.id.search_button);
        searchInput = view.findViewById(R.id.search_input);
        eventListView = view.findViewById(R.id.event_list_view);
        availableOnlyFilter = view.findViewById(R.id.events_filter_available_checkbox);
        capacityOnlyFilter = view.findViewById(R.id.events_filter_capacity_checkbox);

        // Set listeners for search button
        searchButton.setOnClickListener(v -> applyFilters());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });

        // Initialize adapter
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, shownNames);
        eventListView.setAdapter(adapter);
        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            NavController controller = NavHostFragment.findNavController(this);
            Event event = shownEvents.get(position);

            applicantRepository.fetchApplicant(user.getId(), event.getId(),
                    new FirestoreCallbackApplicantReceive() {
                        @Override
                        public void onDataReceived(Applicant applicant) {
                            Bundle args = new Bundle();
                            args.putParcelable("event", event);

                            if (event.getOrganizerId().equals(user.getId())) {
                                controller.navigate(R.id.organizerEventFragment, args);
                            } else {
                                args.putParcelable("applicant", applicant);
                                controller.navigate(R.id.eventDetailsFragment, args);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("EventsListFragment", "Could not retrieve applicant");
                        }
                    });
        });

        availableOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        capacityOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());

        loadEvents();
        loadApplicantCounts();
    }

    /**
     * Loads all events from Firestore so they can be filtered locally.
     */
    private void loadEvents() {
        FirebaseFirestore.getInstance().collection("Events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : snapshot) {
                        Event event = Event.fromMap(document.getId(), document.getData());
                        if (event != null)
                            allEvents.add(event);
                    }
                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching events", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Loads applicant counts for each event so the capacity filter can be applied locally.
     */
    private void loadApplicantCounts() {
        FirebaseFirestore.getInstance().collection("Applicants")
                .get()
                .addOnSuccessListener(snapshot -> {
                    applicantCounts.clear();

                    for (QueryDocumentSnapshot document : snapshot) {
                        String eventId = document.contains("event_id")
                                ? document.getString("event_id")
                                : document.getString("eventId");
                        if (eventId == null) {
                            continue;
                        }
                        int currentCount = applicantCounts.containsKey(eventId)
                                ? applicantCounts.get(eventId)
                                : 0;
                        applicantCounts.put(eventId, currentCount + 1);
                    }

                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching event capacity", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Applies the current keyword and checkbox filters to the loaded event list.
     */
    private void applyFilters() {
        shownEvents.clear();
        shownNames.clear();

        shownEvents.addAll(EventFilterUtil.filterEvents(
                allEvents,
                searchInput.getText().toString(),
                availableOnlyFilter.isChecked(),
                capacityOnlyFilter.isChecked(),
                applicantCounts,
                LocalDateTime.now()
        ));

        for (Event event : shownEvents) {
            shownNames.add(event.getName() == null ? "Untitled event" : event.getName());
        }

        adapter.notifyDataSetChanged();
    }
}
