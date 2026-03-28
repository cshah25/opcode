package com.example.opcodeapp.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventsListFragment extends Fragment {

    private EditText searchInput;
    private ListView eventListView;
    private CheckBox availableOnlyFilter;
    private CheckBox capacityOnlyFilter;

    private ArrayList<Event> allEvents = new ArrayList<>();
    private ArrayList<Event> shownEvents = new ArrayList<>();
    private ArrayList<String> shownNames = new ArrayList<>();
    private Map<String, Integer> applicantCounts = new HashMap<>();

    private User currentUser;

    private ArrayAdapter<String> adapter;

    private ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

    public EventsListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eventslist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton createButton = view.findViewById(R.id.events_create_button);


        currentUser = SessionController.getInstance(requireContext()).getCurrentUser();


        ImageButton menuButton = view.findViewById(R.id.events_menu_button);
        Button searchButton = view.findViewById(R.id.search_button);
        searchInput = view.findViewById(R.id.search_input);
        eventListView = view.findViewById(R.id.event_list_view);
        availableOnlyFilter = view.findViewById(R.id.events_filter_available_checkbox);
        capacityOnlyFilter = view.findViewById(R.id.events_filter_capacity_checkbox);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, shownNames);
        eventListView.setAdapter(adapter);



        createButton.setOnClickListener(v ->
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.EventCreatorFragment));

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilters();
            }
        });

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

        availableOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        capacityOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());

        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Bundle bundle2 = new Bundle();

            Event selected_event = shownEvents.get(position);

            bundle2.putParcelable("event", shownEvents.get(position));
            bundle2.putParcelable("user", currentUser);

            List<Applicant> current_applicant = new ArrayList<>();

            applicantRepository.fetchApplicant(currentUser.getId(), selected_event.getId(), new FirestoreCallbackApplicantReceive() {
                @Override
                public void onDataReceived(Applicant applicant) {
                    current_applicant.add(applicant);
                }

                @Override
                public void onError(Exception e) {
                    NavHostFragment.findNavController(EventsListFragment.this)
                            .navigate(R.id.EntrantEventDetailsFragment, bundle2);
                }
            });

            if (selected_event.getOrganizer().getId().equals(currentUser.getId())) {
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.FinalOrganizerEventFragment, bundle2);
            } else if (current_applicant.get(0).getStatus() == ApplicantStatus.NOT_DRAWN) {
                NavHostFragment.findNavController(EventsListFragment.this).navigate(R.id.eventDetailsFragment, bundle2);
            } else {
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.EventInvitationFragment, bundle2);
            }
        });

        loadEvents();
        loadApplicantCounts();
    }

    private void loadEvents() {
        FirebaseFirestore.getInstance().collection("Events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : snapshot) {
                        Event event = mapEvent(document);
                        if (event != null) {
                            allEvents.add(event);
                        }
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

    private boolean matchesKeyword(Event event, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }

        return containsKeyword(event.getName(), keyword)
                || containsKeyword(event.getDescription(), keyword)
                || containsKeyword(event.getLocation(), keyword);
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(keyword);
    }

    private boolean isRegistrationOpen(Event event) {
        if (event.getRegistrationStart() == null || event.getRegistrationEnd() == null) {
            return false;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return !now.isBefore(event.getRegistrationStart()) && !now.isAfter(event.getRegistrationEnd());
    }

    private boolean hasCapacity(Event event) {
        if (event.getWaitlistLimit() < 0) {
            return true;
        }

        int currentApplicants = applicantCounts.containsKey(event.getId())
                ? applicantCounts.get(event.getId())
                : 0;
        return currentApplicants < event.getWaitlistLimit();
    }

    private Event mapEvent(QueryDocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        try {
            String organizerId = (String) data.get("organizer_id");
            User organizer = User.builder()
                    .id(organizerId == null ? "" : organizerId)
                    .deviceId("")
                    .name("")
                    .email("")
                    .phoneNum("")
                    .isAdmin(false)
                    .build();

            return new Event(
                    document.getId(),
                    (String) data.get("name"),
                    (String) data.get("location"),
                    (String) data.get("description"),
                    DateUtil.fromLong(Long.valueOf(data.get("start").toString())),
                    DateUtil.fromLong(Long.valueOf(data.get("end").toString())),
                    DateUtil.fromLong(Long.valueOf(data.get("registration_start").toString())),
                    DateUtil.fromLong(Long.valueOf(data.get("registration_end").toString())),
                    organizer,
                    data.get("price") == null ? 0.0f : Float.valueOf(data.get("price").toString()),
                    data.get("waitlist_limit") == null ? -1 : Integer.parseInt(data.get("waitlist_limit").toString())
            );
        } catch (Exception e) {
            return null;
        }
    }
}
