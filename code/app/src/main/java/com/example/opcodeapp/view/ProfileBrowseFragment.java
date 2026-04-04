package com.example.opcodeapp.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.UserArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.callback.FirestoreCallbackUsersReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.Repository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileBrowseFragment extends Fragment implements RemoveUserDialogFragment.RemoveUserDialogListener {

    private Button searchButton;
    private EditText searchInput;
    private ListView profileListView;

    private List<User> allUsers = new ArrayList<>();
    private List<User> shownUsers = new ArrayList<>();

    private ArrayAdapter<User> adapter;
    private UserRepository userRepository;
    private User user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if the current user is non-null
        user = SessionController.getInstance(requireContext()).getCurrentUser();
        if (user == null) {
            NavHostFragment.findNavController(this).navigate(R.id.setupFragment);
            Log.e("ProfileBrowseFragment", "Could not retrieve the current user");
            return;
        }


        // Bind views
        searchButton = view.findViewById(R.id.search_button);
        searchInput = view.findViewById(R.id.search_input);
        profileListView = view.findViewById(R.id.profile_list_view);

        // Set listeners for search button
        searchButton.setOnClickListener(v -> applyFilters());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });


        userRepository = new UserRepository(FirebaseFirestore.getInstance());

        userRepository.fetchUsers(null, new FirestoreCallbackUsersReceive() {

            @Override
            public void onDataReceived(List<User> users) {
                allUsers.addAll(users);
                adapter = new UserArrayAdapter(requireContext(), (ArrayList<User>) allUsers);
                profileListView.setAdapter(adapter);
                applyFilters();

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching users", Toast.LENGTH_SHORT).show();
            }

        });

        // Initialize adapter
        profileListView.setOnItemClickListener((parent, itemView, position, id) -> {
            User user = adapter.getItem(position);


            RemoveUserDialogFragment removeUserDialogFragment = RemoveUserDialogFragment.newInstance(user);
            removeUserDialogFragment.show(getParentFragmentManager(), "Remove");
            adapter.notifyDataSetChanged();

        });


    }

    private void applyFilters() {
        String text = searchInput.getText().toString().trim().toLowerCase(Locale.getDefault());


        for (User user : allUsers) {
            if (!matchesKeyword(user, text)) {
                continue;
            }

            shownUsers.add(user);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * A case-insensitive checks for any keyword references that occur within an {@link User}'s name,
     * description or location.
     *
     * @param user   The event to search the keyword for
     * @param keyword The keyword to search for
     * @return {@code true} if any keyword occurs in the user, {@code false} otherwise
     */
    private boolean matchesKeyword(User user, String keyword) {
        if (keyword.isEmpty())
            return true;

        return containsKeyword(user.getName(), keyword)
                || containsKeyword(user.getEmail(), keyword)
                || containsKeyword(user.getPhoneNum(), keyword);
    }

    /**
     * @param value   The string to search through
     * @param keyword The keyword to search for
     * @return {@code true} if the keyword occurs in the string, {@code false} otherwise
     */
    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(keyword);
    }

    @Override
    public void removeUser(User user) {
        UserRepository user_repository = new UserRepository(FirebaseFirestore.getInstance());

        EventRepository event_repository = new EventRepository(FirebaseFirestore.getInstance());

        ApplicantRepository applicant_repository = new ApplicantRepository(FirebaseFirestore.getInstance());

        user_repository.deleteUser(user.getId(), new FirestoreCallbackSend() {

            @Override
            public void onSendSuccess(Void aVoid) {
                Toast.makeText(getContext(), "User removed", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(getContext(), "Error removing user", Toast.LENGTH_SHORT).show();
            }
        });

        event_repository.deleteEventsByOrganizerId(user.getId(), new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {

                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                    }
        });


        applicant_repository.deleteApplicantsByUser(user.getId(), new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error deleting profile", Toast.LENGTH_SHORT).show();
                    }
        });

        shownUsers.remove(user);
        adapter.notifyDataSetChanged();

    }
}
