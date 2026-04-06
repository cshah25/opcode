package com.example.opcodeapp.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

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
        dataList.clear();
        dataList.addAll(EventFilterUtil.filterEvents(
                allEvents,
                searchInput.getText().toString(),
                availableOnlyFilter.isChecked(),
                capacityOnlyFilter.isChecked(),
                user.isAdmin()
        ));

        adapter.notifyDataSetChanged();
    }
}
