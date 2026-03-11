package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.profile_name_input);
        emailInput = view.findViewById(R.id.profile_email_input);
        phoneInput = view.findViewById(R.id.profile_phone_input);

        ImageButton backButton = view.findViewById(R.id.profile_back_button);
        Button updateButton = view.findViewById(R.id.profile_update_button);
        Button deleteButton = view.findViewById(R.id.profile_delete_button);

        User currentUser = SessionController.getInstance(requireContext()).getCurrentUser();

        if (currentUser != null) {
            nameInput.setText(currentUser.getName());
            emailInput.setText(currentUser.getEmail());
            phoneInput.setText(currentUser.getPhoneNum());
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(ProfileFragment.this).navigateUp();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = SessionController.getInstance(requireContext()).getCurrentUser();
                if (user == null) {
                    return;
                }

                user.setName(nameInput.getText().toString().trim());
                user.setEmail(emailInput.getText().toString().trim());
                user.setPhoneNum(phoneInput.getText().toString().trim());

                DBManager db = new DBManager(FirebaseFirestore.getInstance());
                db.updateUser(user, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess() {
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                    }
                });
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = SessionController.getInstance(requireContext()).getCurrentUser();
                if (user == null) {
                    return;
                }

                DBManager db = new DBManager(FirebaseFirestore.getInstance());
                db.deleteProfile(user, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess() {
                        NavHostFragment.findNavController(ProfileFragment.this).navigateUp();
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                    }
                });
            }
        });
    }
}