package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.callback.FirestoreCallbackSend;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.UIValidationUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;

    private Button updateButton;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameLayout = view.findViewById(R.id.profile_name_layout);
        emailLayout = view.findViewById(R.id.profile_email_layout);
        phoneLayout = view.findViewById(R.id.profile_phone_layout);

        nameInput = view.findViewById(R.id.profile_name_input);
        emailInput = view.findViewById(R.id.profile_email_input);
        phoneInput = view.findViewById(R.id.profile_phone_input);

        addErrorClearingWatchers();

        updateButton = view.findViewById(R.id.profile_update_button);
        Button deleteButton = view.findViewById(R.id.profile_delete_button);

        Button browseButton = view.findViewById(R.id.browse_profile_button);

        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
        User user = SessionController.getInstance(requireContext()).getCurrentUser();

        if (user != null) {
            nameInput.setText(user.getName());
            emailInput.setText(user.getEmail());
            phoneInput.setText(user.getPhoneNum());
        }


        /**
         * if the current user is admin then they can browse different profiles.
         */
        browseButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.profileBrowseFragment);
        });

        browseButton.setVisibility((user.isAdmin()) ? View.VISIBLE : View.GONE);


        updateButton.setOnClickListener(v -> {
            // Cancel if user is null
            if (user == null)
                return;

            // Set to edit mode
            if (!isEditMode) {
                setEditable(true);
                return;
            }

            boolean valid = UIValidationUtil.validateRequiredFields(Map.of(
                    nameInput, nameLayout,
                    emailInput, emailLayout
            ));

            if (!valid)
                return;

            // Retrieve text from fields
            String nameText = UIValidationUtil.getText(nameInput);
            String emailText = UIValidationUtil.getText(emailInput);
            String phoneText = UIValidationUtil.getText(phoneInput);

            // Update User instance if field has been changed
            if (!user.getName().equals(nameText))
                user.setName(nameText);

            if (!user.getEmail().equals(emailText))
                user.setEmail(emailText);

            if (!user.getPhoneNum().equals(phoneText))
                user.setPhoneNum(phoneText);

            setEditable(false);

            // Only call update if there was a change to the User instance
            // and remove dirty flag
            if (user.isDirty()) {
                userRepository.updateUser(user, new FirestoreCallbackSend() {

                    @Override
                    public void onSendSuccess(Void aVoid) {
                        user.setDirty(false);
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (user == null)
                return;

            userRepository.deleteUser(user.getId(), new FirestoreCallbackSend() {
                @Override
                public void onSendSuccess(Void aVoid) {
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSendFailure(Exception e) {
                    Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                }
            });

            new EventRepository(FirebaseFirestore.getInstance()).deleteEventsByOrganizerId(user.getId(),
                    new FirestoreCallbackSend() {
                        @Override
                        public void onSendSuccess(Void unused) {

                        }

                        @Override
                        public void onSendFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                        }
            });

            new ApplicantRepository(FirebaseFirestore.getInstance()).deleteApplicantsByUser(user.getId(),
                    new FirestoreCallbackSend() {
                        @Override
                        public void onSendSuccess(Void unused) {
                        }

                        @Override
                        public void onSendFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                        }
                    });

            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.setupFragment);
        });
    }

    /**
     * Attaches watchers to all fields to clear errors when updated
     */
    private void addErrorClearingWatchers() {
        UIValidationUtil.addErrorClearingWatcher(nameInput, nameLayout);
        UIValidationUtil.addErrorClearingWatcher(emailInput, emailLayout);
        UIValidationUtil.addErrorClearingWatcher(phoneInput, phoneLayout);
    }

    /**
     * Toggles the editable
     *
     * @param isEditMode
     */
    private void setEditable(boolean isEditMode) {
        this.isEditMode = isEditMode;
        nameInput.setEnabled(isEditMode);
        emailInput.setEnabled(isEditMode);
        phoneInput.setEnabled(isEditMode);
        updateButton.setText(isEditMode ? "Save" : "Update");
    }
}