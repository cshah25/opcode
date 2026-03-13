package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


/**
 * The fragment for showing the Event information (Organizer Perspective) after registration is complete.
 */
 public class FinalOrganizerEventFragment extends Fragment {
    private Event event;

    public FinalOrganizerEventFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentEventView = inflater.inflate(R.layout.fragment_final_organizer_event, container, false);
        return fragmentEventView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getArguments() != null) {
            event = getArguments().getParcelable("event");
        }

        if (event == null) {
            NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigateUp();
            return;
        }

        User currentUser = getArguments().getParcelable("user");

        ImageButton backButton = view.findViewById(R.id.event_back_button);

        TextView nameText = view.findViewById(R.id.event_name_text);
        TextView dateText = view.findViewById(R.id.event_date_text);
        TextView locationText = view.findViewById(R.id.event_location_text);
        TextView descriptionText = view.findViewById(R.id.event_description_text);


        nameText.setText(event.getName());
        dateText.setText(event.getStart() + " to " + event.getEnd());
        locationText.setText(event.getLocation());
        descriptionText.setText(event.getDescription());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigateUp();
            }
        });




        view.findViewById(R.id.enrolled_users_button).setOnClickListener(new View.OnClickListener() {
            /**
             * The click listener for the enrolled users button.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {

                User[] enrolledUsers = event.getAttendees().toArray(new User[0]);
                FinalOrganizerEventFragmentDirections.ActionFinalOrganizerEventFragmentToEnrolledUsersFragment action = FinalOrganizerEventFragmentDirections.actionFinalOrganizerEventFragmentToEnrolledUsersFragment(enrolledUsers);
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(action);


            }

        });

        view.findViewById(R.id.event_profile_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.ProfileFragment);
            }

        });

        view.findViewById(R.id.invited_users_button).setOnClickListener(new View.OnClickListener() {
            /**
             * The click listener for the invited users button.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putParcelable("event", event);
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.InvitedUsersFragment, bundle);


            }


        });

        view.findViewById(R.id.all_applicants_button).setOnClickListener(new View.OnClickListener() {
            /**
             * The click listener for the all applicants button.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("event", event);
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.ApplicantFragment, bundle);
            }

        });





    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
