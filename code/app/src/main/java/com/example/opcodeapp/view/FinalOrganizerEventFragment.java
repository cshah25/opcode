package com.example.opcodeapp.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * The fragment for showing the Event information (Organizer Perspective) after registration is complete.
 */
public class FinalOrganizerEventFragment extends Fragment {
    private Event event;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_final_organizer_event, container, false);
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            throw new IllegalArgumentException("No arguments passed");

        event = args.getParcelable("event", Event.class);
        if (event == null) {
            NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigateUp();
            return;
        }

        ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        ImageButton backButton = view.findViewById(R.id.event_back_button);
        ImageButton profileButton = view.findViewById(R.id.event_profile_button);
        Button enrolledApplicantButton = view.findViewById(R.id.enrolled_users_button);
        Button invitedApplicantButton = view.findViewById(R.id.invited_users_button);
        Button allApplicantButton = view.findViewById(R.id.all_applicants_button);

        TextView nameText = view.findViewById(R.id.event_name_text);
        TextView dateText = view.findViewById(R.id.event_date_text);
        TextView locationText = view.findViewById(R.id.event_location_text);
        TextView descriptionText = view.findViewById(R.id.event_description_text);
        TextView priceText = view.findViewById(R.id.event_price_text);
        TextView waitListText = view.findViewById(R.id.event_waitlist_count_text);
        TextView registrationText = view.findViewById(R.id.event_reg_close_text);
        TextView eventRegistrationText = view.findViewById(R.id.event_open_closed_text);

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
        if (total != -1) {
            List<Applicant> allApplicants = new ArrayList<>();
            applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {

                @Override
                public void onDataReceived(List<Applicant> applicants) {
                    allApplicants.addAll(applicants);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
                }
            });
            waitListText.setText("Waitlist: " + allApplicants.size() + "/" + total);
        } else {
            waitListText.setText("Waitlist Limit: None");
        }

        backButton.setOnClickListener(v -> NavHostFragment.findNavController(FinalOrganizerEventFragment.this).navigate(R.id.EventListFragment));

        enrolledApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(FinalOrganizerEventFragment.this)
                    .navigate(R.id.EnrolledUsersFragment,  bundle);
        });

        profileButton.setOnClickListener(v ->
                NavHostFragment.findNavController(FinalOrganizerEventFragment.this)
                        .navigate(R.id.ProfileFragment)
        );

        invitedApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(FinalOrganizerEventFragment.this)
                    .navigate(R.id.InvitedUsersFragment, bundle);
        });

        allApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(FinalOrganizerEventFragment.this)
                    .navigate(R.id.WaitListFragment, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
