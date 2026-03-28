package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.callback.FirestoreCallbackSend;

import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
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

                UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
                userRepository.updateUser(user, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void aVoid) {
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
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

                UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
                userRepository.deleteUser(user.getId(), new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void aVoid) {
                        SessionController.getInstance(requireContext()).logout();
                        Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(ProfileFragment.this)
                                .navigate(R.id.action_ProfileFragment_to_setup_graph);
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
