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

import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.navigation.fragment.NavHostFragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventsListFragment extends Fragment {

    private Button searchButton;
    private EditText searchInput;
    private ListView eventListView;
    private CheckBox availableOnlyFilter;
    private CheckBox capacityOnlyFilter;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> dataList = new ArrayList<>();
    private List<String> shownNames = new ArrayList<>();
    private Map<String, Integer> applicantCounts = new HashMap<>();

    private ArrayAdapter<String> adapter;
    private ApplicantRepository applicantRepository;
    private EventRepository eventRepository;
    private User user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

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
        searchButton.setOnClickListener(v -> filterEvents());
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

    private void loadEvents() {

        FirebaseFirestore.getInstance().collection("Events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : snapshot) {
                        // TODO: Use event repository
                        Event event = mapEvent(document);
                        if (event != null)
                            allEvents.add(event);
                    }

                    applyFilters();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching events", Toast.LENGTH_SHORT).show()
                );
    }

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

    private void applyFilters() {
        String text = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());

        shownEvents.clear();
        shownNames.clear();

        for (Event event : allEvents) {
            if (!matchesKeyword(event, text)) {
                continue;
            }
            if (availableOnlyFilter.isChecked() && !isRegistrationOpen(event)) {
                continue;
            }
            if (capacityOnlyFilter.isChecked() && !hasCapacity(event)) {
                continue;
            }

            shownEvents.add(event);
            shownNames.add(event.getName() == null ? "Untitled event" : event.getName());
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * A case-insensitive checks for any keyword references that occur within an {@link Event}'s name,
     * description or location.
     *
     * @param event   The event to search the keyword for
     * @param keyword The keyword to search for
     * @return {@code true} if any keyword occurs in the event, {@code false} otherwise
     */
    private boolean matchesKeyword(Event event, String keyword) {
        if (keyword.isEmpty())
            return true;

        return containsKeyword(event.getName(), keyword)
                || containsKeyword(event.getDescription(), keyword)
                || containsKeyword(event.getLocation(), keyword);
    }

    /**
     * @param value   The string to search through
     * @param keyword The keyword to search for
     * @return {@code true} if the keyword occurs in the string, {@code false} otherwise
     */
    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(keyword);
    }

    /**
     * Checks if the event's registration period is open
     *
     * @param event The event being checked
     * @return {@code true} if the event registration period is open, {@code false} otherwise
     */
    private boolean isRegistrationOpen(Event event) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(event.getRegistrationStart()) && now.isBefore(event.getRegistrationEnd());
    }

    /**
     * Checks if the event has not reached waitlist capacity
     *
     * @param event The event being checked
     * @return {@code true} if the event has not reached the waitlist capacity, {@code false} otherwise
     */
    private boolean hasCapacity(Event event) {
        if (event.getWaitlistLimit() < 0)
            return true;

        int currentApplicants = applicantCounts.containsKey(event.getId())
                ? applicantCounts.get(event.getId())
                : 0;
        return currentApplicants < event.getWaitlistLimit();
    }
}
