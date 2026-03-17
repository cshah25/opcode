package com.example.opcodeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.opcodeapp.model.User;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {
    public UserArrayAdapter(Context context, ArrayList<User> logs) {
        super(context, 0, logs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.enrolled_user_content,
                    parent, false);
        } else {
            view = convertView;
        }
        User user = getItem(position);
        TextView userName = view.findViewById(R.id.user_name_text);
        TextView userEmail = view.findViewById(R.id.user_email_text);
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());

        return view;
    }

}
