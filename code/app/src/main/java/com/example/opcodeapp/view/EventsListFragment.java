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
import com.example.opcodeapp.adapter.EventArrayAdapter;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.util.EventFilterUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsListFragment extends Fragment {

    private Button searchButton;
    private EditText searchInput;
    private ListView eventListView;
    private CheckBox availableOnlyFilter;
    private CheckBox capacityOnlyFilter;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> dataList = new ArrayList<>();

    private EventArrayAdapter adapter;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        adapter = new EventArrayAdapter(requireContext(), dataList);
        eventListView.setAdapter(adapter);
        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            NavController controller = NavHostFragment.findNavController(this);
            Event event = dataList.get(position);

            Bundle args = new Bundle();
            args.putParcelable("event", event);
            int destination = event.getOrganizerId().equals(user.getId()) ?
                    R.id.organizerEventFragment :
                    R.id.eventDetailsFragment;
            controller.navigate(destination, args);
        });

        availableOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        capacityOnlyFilter.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        loadEvents();
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
                        if (event != null) allEvents.add(event);
                    }
                    applyFilters();
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching events", Toast.LENGTH_SHORT).show());
    }

    /**
     * Applies the current keyword and checkbox filters to the loaded event list.
     */
    private void applyFilters() {
//        String text = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());
//        dataList.clear();
//
//        for (Event event : allEvents) {
//            if (!matchesKeyword(event, text)) continue;
//
//            if (availableOnlyFilter.isChecked() && !isRegistrationOpen(event)) continue;
//
//            if (capacityOnlyFilter.isChecked() && !hasCapacity(event)) continue;
//
//            if (!event.isPublic() && !user.isAdmin()) continue;
//
//            dataList.add(event);
//        }

        shownEvents.clear();
        shownNames.clear();

        dataList.addAll(EventFilterUtil.filterEvents(
                        allEvents,
                        searchInput.getText().toString(),
                        availableOnlyFilter.isChecked(),
                        capacityOnlyFilter.isChecked(),
                        ,
                        LocalDateTime.now()
                )
        );


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

    /**
     * A case-insensitive checks for any keyword references that occur within an {@link Event}'s name,
     * description or location.
     *
     * @param event   The event to search the keyword for
     * @param keyword The keyword to search for
     * @return {@code true} if any keyword occurs in the event, {@code false} otherwise
     */
    private boolean matchesKeyword(Event event, String keyword) {
        if (keyword.isEmpty()) return true;

        return containsKeyword(event.getName(), keyword) || containsKeyword(event.getDescription(), keyword) || containsKeyword(event.getLocation(), keyword);
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
        if (event.getWaitlistLimit() < 0) return true;

        return event.getWaitlistCount() < event.getWaitlistLimit();
    }

}
