package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fulfills US 01.02.02: Entrant can update name, email, and contact info.
 */
public class ProfileFragment extends Fragment {

    private User currentUser;
    private DBManager dbManager;
    private EditText editName, editEmail, editPhone;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = new DBManager(FirebaseFirestore.getInstance());

        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("CURRENT_USER");
        }

        editName = view.findViewById(R.id.edit_profile_name);
        editEmail = view.findViewById(R.id.edit_profile_email);
        editPhone = view.findViewById(R.id.edit_profile_phone);
        Button btnSave = view.findViewById(R.id.btn_save_profile);

        // Pre-fill the fields with current data
        if (currentUser != null) {
            editName.setText(currentUser.getName());
            editEmail.setText(currentUser.getEmail());
        }

        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void saveProfileData() {
        if (currentUser == null) return;

        // Update the local object
        currentUser.setName(editName.getText().toString());
        currentUser.setEmail(editEmail.getText().toString());
        currentUser.setPhoneNum(editPhone.getText().toString());

        // Push to Firestore
        dbManager.updateUser(currentUser, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess() {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}