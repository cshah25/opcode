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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventsReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventsListFragment extends Fragment {

    private EditText searchInput;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> dataList = new ArrayList<>();
    private final List<String> shownNames = new ArrayList<>();

    private ArrayAdapter<String> adapter;
    private ApplicantRepository applicantRepository;
    private User user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = SessionController.getInstance(requireContext()).getCurrentUser();
        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

        // Bind views
        Button searchButton = view.findViewById(R.id.search_button);
        searchInput = view.findViewById(R.id.search_input);
        ListView eventListView = view.findViewById(R.id.event_list_view);

        // Load the events list
        loadEvents();

        // Set the list view adapter
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, shownNames);
        eventListView.setAdapter(adapter);

        // Call filterEvents when the search button is clicked
        searchButton.setOnClickListener(v -> filterEvents());

        // Navigate to the appropriate view when an item is clicked on the list
        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            NavController controller = NavHostFragment.findNavController(this);
            Event event = dataList.get(position);
            applicantRepository.fetchApplicant(user.getId(), event.getId(),
                    new FirestoreCallbackApplicantReceive() {
                        @Override
                        public void onDataReceived(Applicant applicant) {
                            Bundle args = new Bundle();
                            args.putParcelable("event", event);
                            Log.d("EventList", event.toString());
                            Log.d("EventList", user.toString());
                            Log.d("EventList", event.getOrganizerId());


                            if (event.getOrganizerId().equals(user.getId())) {
                                controller.navigate(R.id.organizerEventFragment, args);
                            } else {
                                args.putParcelable("applicant", applicant);
                                controller.navigate(R.id.entrantEventFragment, args);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("EventsListFragment", "Could not retrieve applicant");
                        }
                    });
        });
    }

    private void loadEvents() {
        EventRepository repository = new EventRepository(FirebaseFirestore.getInstance());
        repository.fetchEvents(null, new FirestoreCallbackEventsReceive() {
            @Override
            public void onDataReceived(List<Event> events) {
                allEvents.clear();
                dataList.clear();
                shownNames.clear();

                if (events != null && !events.isEmpty()) {
                    allEvents.addAll(events);
                    dataList.addAll(events);

                    for (Event event : events)
                        shownNames.add(event.getName());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEvents() {
        String text = searchInput.getText().toString().trim().toLowerCase();

        dataList.clear();
        shownNames.clear();

        for (Event event : allEvents) {
            String name = event.getName();

            if (name != null && name.toLowerCase().contains(text)) {
                dataList.add(event);
                shownNames.add(name);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
