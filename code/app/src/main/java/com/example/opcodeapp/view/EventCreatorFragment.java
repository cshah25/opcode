package com.example.opcodeapp.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.EventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A simple fragment that handles the input validation including dates with date pickers,
 * strings and numbers for creating {@link Event}. Successful event creation also saves
 * it to the Firestore database using {@link EventRepository}
 */
public class EventCreatorFragment extends Fragment {
    private EventCreatorFragment instance;

    private TextInputLayout nameLayout;
    private TextInputLayout locationLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout registrationStartLayout;
    private TextInputLayout registrationEndLayout;
    private TextInputLayout priceLayout;
    private TextInputLayout waitlistLayout;
    private TextInputLayout eventStartLayout;
    private TextInputLayout eventEndLayout;

    private TextInputEditText nameInput;
    private TextInputEditText locationInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText registrationStartInput;
    private TextInputEditText registrationEndInput;
    private TextInputEditText priceInput;
    private TextInputEditText waitlistInput;
    private TextInputEditText eventStartInput;
    private TextInputEditText eventEndInput;

    private MaterialButton createButton;
    private MaterialButton uploadButton;

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);

    private final ActivityResultLauncher<String> posterPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Toast.makeText(requireContext(), "Poster selected", Toast.LENGTH_SHORT).show();
                }
            });

    public EventCreatorFragment() {
        super(R.layout.fragment_event_creator);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        instance = this;
        createButton = view.findViewById(R.id.event_creator_submit_btn);
        uploadButton = view.findViewById(R.id.event_creator_upload_btn);
        bindViews(view);
        addErrorClearingWatchers();

        configureDateField(registrationStartLayout, registrationStartInput, "Select registration start");
        configureDateField(registrationEndLayout, registrationEndInput, "Select registration end");
        configureDateField(eventStartLayout, eventStartInput, "Select event start");
        configureDateField(eventEndLayout, eventEndInput, "Select event end");

        uploadButton.setOnClickListener(v -> posterPickerLauncher.launch("image/*"));
        createButton.setOnClickListener(v -> submitForm());
    }

    /**
     * Binds all components  to the view
     *
     * @param view instance of the view
     */
    private void bindViews(@NonNull View view) {
        nameLayout = view.findViewById(R.id.event_creator_name_layout);
        locationLayout = view.findViewById(R.id.event_creator_location_layout);
        descriptionLayout = view.findViewById(R.id.event_creator_description_layout);
        registrationStartLayout = view.findViewById(R.id.event_creator_registration_start_layout);
        registrationEndLayout = view.findViewById(R.id.event_creator_registration_end_layout);
        priceLayout = view.findViewById(R.id.event_creator_price_layout);
        waitlistLayout = view.findViewById(R.id.event_creator_waitlist_layout);
        eventStartLayout = view.findViewById(R.id.event_creator_start_layout);
        eventEndLayout = view.findViewById(R.id.event_creator_end_layout);

        nameInput = view.findViewById(R.id.event_creator_name_input);
        locationInput = view.findViewById(R.id.event_creator_location_input);
        descriptionInput = view.findViewById(R.id.event_creator_description_input);
        registrationStartInput = view.findViewById(R.id.event_creator_registration_start_input);
        registrationEndInput = view.findViewById(R.id.event_creator_registration_end_input);
        priceInput = view.findViewById(R.id.event_creator_price_input);
        waitlistInput = view.findViewById(R.id.event_creator_waitlist_input);
        eventStartInput = view.findViewById(R.id.event_creator_start_input);
        eventEndInput = view.findViewById(R.id.event_creator_end_input);
    }

    /**
     * Attaches watchers to all fields to clear errors when updated
     */
    private void addErrorClearingWatchers() {
        addErrorClearingWatcher(nameInput, nameLayout);
        addErrorClearingWatcher(locationInput, locationLayout);
        addErrorClearingWatcher(descriptionInput, descriptionLayout);
        addErrorClearingWatcher(registrationStartInput, registrationStartLayout);
        addErrorClearingWatcher(registrationEndInput, registrationEndLayout);
        addErrorClearingWatcher(priceInput, priceLayout);
        addErrorClearingWatcher(waitlistInput, waitlistLayout);
        addErrorClearingWatcher(eventStartInput, eventStartLayout);
        addErrorClearingWatcher(eventEndInput, eventEndLayout);
    }

    /**
     * Clears the error hint messages when the text is being updated
     *
     * @param layout The text input layout
     * @param input  The text input text field
     */
    private void addErrorClearingWatcher(TextInputEditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Configures date inputs to open the date picker when selected and disables manual input
     *
     * @param layout The text input layout
     * @param input  The text input text field
     * @param title  Title of the date picker
     */
    private void configureDateField(TextInputLayout layout, TextInputEditText input, String title) {
        layout.setEndIconOnClickListener(v -> openDatePicker(input, title));
        input.setOnClickListener(v -> openDatePicker(input, title));
        input.setKeyListener(null);
    }

    /**
     * Initializes the date picker to attach to date field listeners
     *
     * @param targetInput The text input of the date field
     * @param title       The title of the date picker
     */
    private void openDatePicker(TextInputEditText targetInput, String title) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title);

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                LocalDateTime dateTime = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                targetInput.setText(dateFormat.format(dateTime));
            }
        });
        picker.show(getParentFragmentManager(), title);
    }

    /**
     * Handles validation of input parameters before creating new Event. It returns errors in the ui
     * for any failing validation tests. It then saves to the Firestore database and navigates
     * forward to the event details screen.
     */
    private void submitForm() {
        clearAllErrors();
        createButton.setEnabled(false);

        String name = getText(nameInput);
        String location = getText(locationInput);
        String description = getText(descriptionInput);
        String registrationStart = getText(registrationStartInput);
        String registrationEnd = getText(registrationEndInput);
        String priceText = getText(priceInput);
        String waitlistText = getText(waitlistInput);
        String eventStart = getText(eventStartInput);
        String eventEnd = getText(eventEndInput);

        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError("Required");
            valid = false;
        }

        if (location.isEmpty()) {
            locationLayout.setError("Required");
            valid = false;
        }

        if (description.isEmpty()) {
            descriptionLayout.setError("Required");
            valid = false;
        }

        if (registrationStart.isEmpty()) {
            registrationStartLayout.setError("Required");
            valid = false;
        }

        if (registrationEnd.isEmpty()) {
            registrationEndLayout.setError("Required");
            valid = false;
        }

        if (eventStart.isEmpty()) {
            eventStartLayout.setError("Required");
            valid = false;
        }

        if (eventEnd.isEmpty()) {
            eventEndLayout.setError("Required");
            valid = false;
        }

        Float price = null;
        if (!priceText.isEmpty()) {
            try {
                price = Float.valueOf(priceText);
                if (price < 0) {
                    priceLayout.setError("Must be 0 or greater");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                priceLayout.setError("Enter a valid price");
                valid = false;
            }
        } else {
            price = 0.0f;
        }

        Integer waitlistLimit = null;
        if (!waitlistText.isEmpty()) {
            try {
                waitlistLimit = Integer.parseInt(waitlistText);
                if (waitlistLimit < 0) {
                    waitlistLayout.setError("Must be 0 or greater");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                waitlistLayout.setError("Enter a valid whole number");
                valid = false;
            }
        } else {
            waitlistLimit = -1;
        }

        if (!valid)
            return;


        Toast.makeText(requireContext(), "Event created", Toast.LENGTH_SHORT).show();
        EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());
        SessionController controller = SessionController.getInstance(getContext());
        User organizer = controller.getCurrentUser();

        //Event(id, name, location, description, registrationStart, registrationEnd, start, end, organizer, price, waitlistLimit)
        Event.Builder builder = Event.builder()
                .name(name)
                .location(location)
                .description(description)
                .registrationStart(LocalDate.parse(eventStart, dateFormat).atStartOfDay())
                .registrationEnd(LocalDate.parse(eventEnd, dateFormat).atStartOfDay())
                .start(LocalDate.parse(eventStart, dateFormat).atStartOfDay())
                .end(LocalDate.parse(eventEnd, dateFormat).atStartOfDay())
                .organizer(organizer);
        Event event = builder.build();
        eventRepository.addEvent(event, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                createButton.setEnabled(true);

                Bundle args = new Bundle();
                args.putParcelable("event", event);
                NavHostFragment.findNavController(instance)
                        .navigate(R.id.action_EventCreatorFragment_to_FinalOrganizerEventFragment, args);
                Toast.makeText(getContext(), "Created event successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendFailure(Exception e) {
                createButton.setEnabled(true);
                Toast.makeText(getContext(), "Error creating event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clears all error hints from the text input layouts
     */
    private void clearAllErrors() {
        nameLayout.setError(null);
        locationLayout.setError(null);
        descriptionLayout.setError(null);
        registrationStartLayout.setError(null);
        registrationEndLayout.setError(null);
        priceLayout.setError(null);
        waitlistLayout.setError(null);
        eventStartLayout.setError(null);
        eventEndLayout.setError(null);
    }

    /**
     * @return the text contained in the text input fields
     */
    private String getText(TextInputEditText input) {
        Editable editable = input.getText();
        return editable == null ? "" : editable.toString().trim();
    }
}
