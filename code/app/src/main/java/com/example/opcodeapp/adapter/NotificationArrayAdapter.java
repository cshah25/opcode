package com.example.opcodeapp.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.NotificationRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Display a single notification
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    NotificationRepository notificationRepository;

    public NotificationArrayAdapter(Context context, List<Notification> items) {
        super(context, 0, items);
        Log.i("NotificationArrayAdapter", "Got " + items.get(0) + " items");
        notificationRepository = new NotificationRepository(FirebaseFirestore.getInstance());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_content,
                    parent, false);
        } else {
            view = convertView;
        }

        Notification notification = getItem(position);
        if (notification == null) {
            Log.e("NotificationArrayAdapter", "Notification is null");
            return view;
        }
        // display a coloured/non coloured dot to indicate if the noti has been read
        TextView read = view.findViewById(R.id.notification_read);
        if (notification.isRead()) {
            Drawable circle = ContextCompat.getDrawable(view.getContext(), R.drawable.dull_circle);
            read.setBackground(circle);
        } else {
            // now that we've read the notification, we can mark it as read
            notification.setRead(true);
            notificationRepository.updateNotification(notification, new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {

                }

                @Override
                public void onSendFailure(Exception e) {
                    Log.e("NotificationArrayAdapter", "Couldn't mark notification as read", e);
                }
            });
        }

        // get the name of the event associated with this notification
        EventRepository repo = new EventRepository(FirebaseFirestore.getInstance());
        TextView event_name = view.findViewById(R.id.notification_event_name);
        // make it look like a link
        event_name.setPaintFlags(event_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        repo.fetchEvent(notification.getEvent_id(), new FirestoreCallbackEventReceive() {
            @Override
            public void onDataReceived(@Nullable Event e) {
                if (e == null) {
                    Toast.makeText(getContext(), "Associated event doesn't exist", Toast.LENGTH_SHORT).show();
                    Log.e("NotificationArrayAdapter", "Associated event doesn't exist.");
                    return;
                }
                event_name.setText(e.getName());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching event name", Toast.LENGTH_SHORT).show();
                Log.e("NotificationArrayAdapter", "Couldn't fetch event name", e);
            }
        });

        // display noti message
        TextView body = view.findViewById(R.id.notification_body);
        body.setText(notification.getBody());

        // create delete button for this notification
        Button delete_button = view.findViewById(R.id.notification_delete);
        delete_button.setOnClickListener(v -> {
            notificationRepository.deleteNotification(notification.getId(), new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void unused) {
                    Log.i("NotificationArrayAdapter", "deleted notification");
                    Toast.makeText(getContext(), "Notification cleared", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(getContext(), "Error deleting notification", Toast.LENGTH_SHORT).show();
                    Log.e("NotificationArrayAdapter", "Couldn't delete noti", e);
                }
            });
            // update the view side too
            this.remove(notification);
        });

        return view;
    }
}
