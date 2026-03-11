package com.example.opcodeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;

public class InvitedUserArrayAdapter extends ArrayAdapter<User> {

    private Event event;

    public InvitedUserArrayAdapter(Context context, ArrayList<User> users, Event event) {
        super(context, 0, users);

        this.event = event;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.invited_user_content,
                    parent, false);
        } else {
            view = convertView;
        }
        User user = getItem(position);
        TextView userName = view.findViewById(R.id.user_name_text);
        TextView userEmail = view.findViewById(R.id.user_email_text);
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());

        if (event.getInvited().contains(user)) {
            TextView userStatus = view.findViewById(R.id.user_status_text);
            userStatus.setText("Invited");
        } else if (event.getAttendees().contains(user)) {
            TextView userStatus = view.findViewById(R.id.user_status_text);
            userStatus.setText("Accepted");

        } else if (event.getDeclined().contains(user)) {
            TextView userStatus = view.findViewById(R.id.user_status_text);
            userStatus.setText("Declined");
        }



        return view;
    }

}
