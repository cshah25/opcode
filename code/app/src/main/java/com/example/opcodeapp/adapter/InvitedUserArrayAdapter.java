package com.example.opcodeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.opcodeapp.R;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;

import java.util.ArrayList;

public class InvitedUserArrayAdapter extends ArrayAdapter<User> {

    private final Applicant applicant;

    public InvitedUserArrayAdapter(Context context, ArrayList<User> users, Applicant applicant) {
        super(context, 0, users);
        this.applicant = applicant;
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

        TextView userStatus = view.findViewById(R.id.user_status_text);
        switch (applicant.getStatus()) {
            case INVITED:
                userStatus.setText("Invited");
                break;
            case ACCEPTED:
                userStatus.setText("Accepted");
                break;
            case DECLINED:
                userStatus.setText("Declined");
                break;
        }
        return view;
    }

}
