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
import com.example.opcodeapp.model.User;

import java.util.ArrayList;


/**
 * The ArrayAdapter for the list of users who are enrolled in an event.
 */
public class EnrolledUserArrayAdapter extends ArrayAdapter<User> {
    public EnrolledUserArrayAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
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
