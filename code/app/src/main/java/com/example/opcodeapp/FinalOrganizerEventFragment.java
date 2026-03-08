package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.databinding.FragmentFinalOrganizerEventBinding;

public class FinalOrganizerEventFragment extends Fragment {

    private FragmentFinalOrganizerEventBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentEventView = inflater.inflate(R.layout.fragment_final_organizer_event, container, false);
        return fragmentEventView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.enrolled_users_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User[] userList;
                FinalOrganizerEventFragmentDirections.ActionFinalOrganizerEventFragmentToEnrolledUsersFragment action = FinalOrganizerEventFragmentDirections.actionFinalOrganizerEventFragmentToEnrolledUsersFragment(userList);
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(action);

            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
