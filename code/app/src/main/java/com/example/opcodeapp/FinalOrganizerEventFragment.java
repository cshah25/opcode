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

import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.util.DateUtil;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;


/**
 * The fragment for showing the Event information (Organizer Perspective) after registration is complete.
 */
public class FinalOrganizerEventFragment extends Fragment {
    private Event event;
    private TextView nameText;
    private TextView dateText;
    private TextView locationText;
    private TextView descriptionText;
    private TextView priceText;
    private TextView waitListText;
    private TextView registrationText;
    private TextView eventRegistrationText;

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

        ImageButton backButton = view.findViewById(R.id.event_back_button);

        nameText = view.findViewById(R.id.event_name_text);
        dateText = view.findViewById(R.id.event_date_text);
        locationText = view.findViewById(R.id.event_location_text);
        descriptionText = view.findViewById(R.id.event_description_text);
        priceText = view.findViewById(R.id.event_price_text);
        waitListText = view.findViewById(R.id.event_waitlist_count_text);
        registrationText = view.findViewById(R.id.event_reg_close_text);
        eventRegistrationText = view.findViewById(R.id.event_open_closed_text);

        nameText.setText(event.getName());
        dateText.setText("Date: " + event.getStart() + " to " + event.getEnd());
        locationText.setText("Location: " + event.getLocation());
        descriptionText.setText("Description: \n" + event.getDescription());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime registrationStart = event.getRegistrationStart();
        LocalDateTime registrationEnd = event.getRegistrationEnd();

        registrationText.setText("Registration Period: " + DateUtil.toString(registrationStart) + "to " + DateUtil.toString(registrationEnd));

        if (registrationStart.isAfter(now) || registrationEnd.isBefore(now)) {
            eventRegistrationText.setText("CLOSED");
        } else {
            eventRegistrationText.setText("OPENED");
        }

        float price = event.getPrice();
        if (price > 0) {
            priceText.setText("Price: Free");
        } else {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CANADA);
            priceText.setText("Price: " + format.format(price));
        }

        int total = event.getWaitlistLimit();
        if (total == -1) {
            waitListText.setText("Waitlist Limit: None");
        } else {
            int currentWaitlist = event.getInitialApplicants().size();
            waitListText.setText("Waitlist Limit: " + currentWaitlist + "/" + total);
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.eventsFragment);
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
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.WaitListFragment, bundle);
            }

        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
