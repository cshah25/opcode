package com.example.opcodeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.opcodeapp.R;
import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackUserReceive;
import com.example.opcodeapp.db.FirestoreCallbackUsersReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<Applicant> {
    public UserArrayAdapter(Context context, ArrayList<Applicant> logs) {
        super(context, 0, logs);
    }

    private DBManager dbManager = new DBManager(FirebaseFirestore.getInstance());


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
        Applicant user = getItem(position);
        TextView userName = view.findViewById(R.id.user_name_text);
        TextView userEmail = view.findViewById(R.id.user_email_text);
        userName.setText(user.getName());

        dbManager.fetchUserById(user.getUserId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User u) {
                userEmail.setText(u.getEmail());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching user", Toast.LENGTH_SHORT).show();

            }
        });

        return view;
    }

}
