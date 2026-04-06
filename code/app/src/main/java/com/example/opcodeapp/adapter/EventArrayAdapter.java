package com.example.opcodeapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    public EventArrayAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_list_content,
                    parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);

        TextView eventName = view.findViewById(R.id.tv_event_item_name);
        TextView eventDescription = view.findViewById(R.id.tv_event_item_description);
        TextView eventBadge = view.findViewById(R.id.tv_event_item_badge);
        TextView eventOrganizer = view.findViewById(R.id.tv_event_item_organizer);

        eventName.setText(event.getName());
        eventDescription.setText(event.getDescription());

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(event.getEnd())) {
            eventBadge.setText("Ended");
            eventBadge.setBackgroundResource(R.color.badgeNegative);
        } else if (now.isAfter(event.getRegistrationEnd()) && now.isBefore(event.getStart())) {
            eventBadge.setText("Closed");
            eventBadge.setBackgroundResource(R.color.badgeNegative);
        } else if (event.getWaitlistLimit() != -1 && event.getWaitlistCount() >= event.getWaitlistLimit()) {
            eventBadge.setText("Full");
            eventBadge.setBackgroundResource(R.color.badgeNeutral);
        } else {
            eventBadge.setText("Open");
            eventBadge.setBackgroundResource(R.color.badgePositive);
        }

        UserRepository repository = new UserRepository(FirebaseFirestore.getInstance());
        repository.fetchUser(event.getOrganizerId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(@Nullable User user) {
                if (user != null)
                    eventOrganizer.setText(user.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e("EventArrayAdapter", "Failed to fetch user document");
            }
        });

        return view;
    }
}
