package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackEventsReceive;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventsListFragment extends Fragment {

    private EditText searchInput;
    private ListView eventListView;

    private ArrayList<Event> allEvents = new ArrayList<>();
    private ArrayList<Event> shownEvents = new ArrayList<>();
    private ArrayList<String> shownNames = new ArrayList<>();

    private User currentUser;

    private ArrayAdapter<String> adapter;

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
                filterEvents();
            }
        });

        eventListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Bundle bundle2 = new Bundle();

            Event selected_event = shownEvents.get(position);

            bundle2.putParcelable("event", shownEvents.get(position));
            bundle2.putParcelable("user", currentUser);

            if (selected_event.getInitialApplicants().contains(currentUser)) {
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.eventDetailsFragment, bundle2);

            } else if (selected_event.getInvited().contains(currentUser)) {
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.EventInvitationFragment, bundle2);
            } else if (selected_event.getOrganizer().equals(currentUser)) {
                NavHostFragment.findNavController(EventsListFragment.this)
                        .navigate(R.id.FinalOrganizerEventFragment, bundle2);
            }  else {
                    NavHostFragment.findNavController(EventsListFragment.this)
                            .navigate(R.id.EntrantEventDetailsFragment, bundle2);

            }

        });

        loadEvents();
    }

    private void loadEvents() {
        DBManager db = new DBManager(FirebaseFirestore.getInstance());
        db.fetchEvents(new FirestoreCallbackEventsReceive() {
            @Override
            public void onDataReceived(List<Event> events) {
                allEvents.clear();
                shownEvents.clear();
                shownNames.clear();

                if (events != null) {
                    allEvents.addAll(events);
                    shownEvents.addAll(events);

                    for (Event event : events) {
                        shownNames.add(event.getName());
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void filterEvents() {
        String text = searchInput.getText().toString().trim().toLowerCase();

        shownEvents.clear();
        shownNames.clear();

        for (Event event : allEvents) {
            String name = event.getName();

            if (name != null && name.toLowerCase().contains(text)) {
                shownEvents.add(event);
                shownNames.add(name);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
