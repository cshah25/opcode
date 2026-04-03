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
public class OrganizerEventFragment extends Fragment {
    private Event event;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_organizer, container, false);
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            throw new IllegalArgumentException("No arguments passed");

        event = args.getParcelable("event", Event.class);
        if (event == null) {
            NavHostFragment.findNavController(OrganizerEventFragment.this).navigateUp();
            return;
        }

        ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        ImageButton commentButton = view.findViewById(R.id.btn_comment);
        Button enrolledApplicantButton = view.findViewById(R.id.enrolled_users_button);
        Button invitedApplicantButton = view.findViewById(R.id.invited_users_button);
        Button allApplicantButton = view.findViewById(R.id.all_applicants_button);
        Button qrCodeButton = view.findViewById(R.id.btn_qr_code);


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


                /**
                 * Called when the applicants are fetched from the database.
                 *Stores the applicants in a list.

                 * @param applicants
                 * The list of applicants.
                 */
                @Override
                public void onDataReceived(List<Applicant> applicants) {
                    allApplicants.addAll(applicants);
                }


                /**
                 * In case of error, display a toast message.
                 *
                 * @param e
                 * The exception that occurred.
                 */
                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
                }
            });
            waitListText.setText("Waitlist: " + allApplicants.size() + "/" + total);
        } else {
            waitListText.setText("Waitlist Limit: None");
        }


        /**
         * Set up the click listeners for the comment button.
         * Sends the user to the Comment Section.
         *
         */
        commentButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(OrganizerEventFragment.this)
                    .navigate(R.id.commentsFragment, bundle);

        });

        /**
         * Set up the click listeners for the enrolled users button.
         * Sends the user to the Enrolled Users Section.
         *
         * @param v
         * The view that was clicked.
         */
        enrolledApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(OrganizerEventFragment.this)
                    .navigate(R.id.enrolledUsersFragment,  bundle);
        });

        invitedApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(OrganizerEventFragment.this)
                    .navigate(R.id.invitedUsersFragment, bundle);
        });

        /**
         * Set up the click listeners for the all applicants button.
         * Sends the user to the All Applicants Section.
         *
         * @param v
         * The view that was clicked.
         */
        allApplicantButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", event);
            NavHostFragment.findNavController(OrganizerEventFragment.this)
                    .navigate(R.id.waitListFragment, bundle);
        });

        // Opens up the QR Code view
        qrCodeButton.setOnClickListener(v ->
                QrCodeViewerFragment.newInstance(event.getId())
                        .show(getParentFragmentManager(), "qr_code_view"));
    }

    /**
     * Called when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
