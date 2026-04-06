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

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.PhoneFormatterWatcher;
import com.example.opcodeapp.util.ValidationUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Displays and edits the current user's profile details.
 */
public class ProfileFragment extends Fragment {

    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;

    private Button updateButton;
    private boolean isEditMode = false;

    /**
     * Inflates the profile screen layout.
     *
     * @param inflater           the layout inflater for this fragment
     * @param container          the parent view that will host the fragment
     * @param savedInstanceState the previously saved state, if any
     * @return the inflated profile screen
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Binds the profile form, populates the current user, and wires the update and delete actions.
     *
     * @param view               the fragment root view
     * @param savedInstanceState the previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameLayout = view.findViewById(R.id.profile_name_layout);
        emailLayout = view.findViewById(R.id.profile_email_layout);
        phoneLayout = view.findViewById(R.id.profile_phone_layout);

        nameInput = view.findViewById(R.id.profile_name_input);
        emailInput = view.findViewById(R.id.profile_email_input);
        phoneInput = view.findViewById(R.id.profile_phone_input);

        phoneInput.addTextChangedListener(new PhoneFormatterWatcher("CA"));

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
        browseButton.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.profileBrowseFragment)
        );

        browseButton.setVisibility((user != null && user.isAdmin()) ? View.VISIBLE : View.GONE);

        updateButton.setOnClickListener(v -> {
            // Cancel if user is null
            if (user == null)
                return;

            // Set to edit mode
            if (!isEditMode) {
                setEditable(true);
                return;
            }

            boolean valid = ValidationUtil.validateRequiredFields(Map.of(
                    nameInput, nameLayout,
                    emailInput, emailLayout
            ));

            // Retrieve text from fields
            String nameText = ValidationUtil.getText(nameInput);
            String emailText = ValidationUtil.getText(emailInput);
            String phoneText = ValidationUtil.getText(phoneInput);

            if (!ValidationUtil.isValidPhoneNumber(phoneText)) {
                phoneLayout.setError("Invalid Phone Number");
                valid = false;
            }

            if (!ValidationUtil.isValidEmail(emailText)) {
                emailLayout.setError("Invalid Email");
                valid = false;
            }

            if (!valid)
                return;

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

            deleteProfile(user, userRepository);
        });
    }

    /**
     * Deletes the current user's profile together with their related events and applications.
     *
     * @param user           the user being deleted
     * @param userRepository the repository used to delete the user profile document
     */
    private void deleteProfile(@NonNull User user, @NonNull UserRepository userRepository) {
        int[] remainingDeletes = {3};
        boolean[] finished = {false};

        userRepository.deleteUser(user.getId(), new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                handleProfileDeletionSuccess(remainingDeletes, finished);
            }

            @Override
            public void onSendFailure(Exception e) {
                handleProfileDeletionFailure(finished);
            }
        });

        new EventRepository(FirebaseFirestore.getInstance()).deleteEventsByOrganizerId(user.getId(),
                new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {
                        handleProfileDeletionSuccess(remainingDeletes, finished);
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        handleProfileDeletionFailure(finished);
                    }
                });

        new ApplicantRepository(FirebaseFirestore.getInstance()).deleteApplicantsByUser(user.getId(),
                new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {
                        handleProfileDeletionSuccess(remainingDeletes, finished);
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        handleProfileDeletionFailure(finished);
                    }
                });
    }

    /**
     * Tracks one successful deletion step and finishes the profile-deletion flow once all related
     * records have been removed.
     *
     * @param remainingDeletes a mutable counter storing how many delete operations are still pending
     * @param finished         a mutable flag used to ignore duplicate callbacks after completion
     */
    private void handleProfileDeletionSuccess(@NonNull int[] remainingDeletes, @NonNull boolean[] finished) {
        if (finished[0]) {
            return;
        }

        remainingDeletes[0]--;
        if (remainingDeletes[0] == 0) {
            finished[0] = true;
            SessionController.getInstance(requireContext()).logout();
            Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(ProfileFragment.this).navigate(R.id.setupFragment);
        }
    }

    /**
     * Ends the profile-deletion flow after the first failed delete callback.
     *
     * @param finished a mutable flag used to ignore duplicate callbacks after a terminal result
     */
    private void handleProfileDeletionFailure(@NonNull boolean[] finished) {
        if (finished[0]) {
            return;
        }

        finished[0] = true;
        Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
    }

    /**
     * Attaches watchers to all fields to clear errors when updated.
     */
    private void addErrorClearingWatchers() {
        ValidationUtil.addErrorClearingWatcher(nameInput, nameLayout);
        ValidationUtil.addErrorClearingWatcher(emailInput, emailLayout);
        ValidationUtil.addErrorClearingWatcher(phoneInput, phoneLayout);
    }

    /**
     * Toggles whether the profile fields are editable.
     *
     * @param isEditMode {@code true} to allow editing, {@code false} to lock the fields
     */
    private void setEditable(boolean isEditMode) {
        this.isEditMode = isEditMode;
        nameInput.setEnabled(isEditMode);
        emailInput.setEnabled(isEditMode);
        phoneInput.setEnabled(isEditMode);
        updateButton.setText(isEditMode ? "Save" : "Update");
    }
}