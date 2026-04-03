package com.example.opcodeapp.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.BuildConfig;
import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.util.UIValidationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private MaterialAutoCompleteTextView locationInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText registrationStartInput;
    private TextInputEditText registrationEndInput;
    private TextInputEditText priceInput;
    private TextInputEditText waitlistInput;
    private TextInputEditText eventStartInput;
    private TextInputEditText eventEndInput;
    private Map<EditText, TextInputLayout> requiredFields;

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

    // Location prediction
    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable currentRequest;
    private ArrayAdapter<String> locationAdapter;
    private Call currentCall;

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

        configureLocationField();

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

        requiredFields = Map.of(
                nameInput, nameLayout,
                locationInput, locationLayout,
                descriptionInput, descriptionLayout,
                registrationStartInput, registrationStartLayout,
                registrationEndInput, registrationEndLayout,
                eventStartInput, eventStartLayout,
                eventEndInput, eventEndLayout
        );
    }

    /**
     * Attaches watchers to all fields to clear errors when updated
     */
    private void addErrorClearingWatchers() {
        UIValidationUtil.addErrorClearingWatcher(nameInput, nameLayout);
        UIValidationUtil.addErrorClearingWatcher(locationInput, locationLayout);
        UIValidationUtil.addErrorClearingWatcher(descriptionInput, descriptionLayout);
        UIValidationUtil.addErrorClearingWatcher(registrationStartInput, registrationStartLayout);
        UIValidationUtil.addErrorClearingWatcher(registrationEndInput, registrationEndLayout);
        UIValidationUtil.addErrorClearingWatcher(priceInput, priceLayout);
        UIValidationUtil.addErrorClearingWatcher(waitlistInput, waitlistLayout);
        UIValidationUtil.addErrorClearingWatcher(eventStartInput, eventStartLayout);
        UIValidationUtil.addErrorClearingWatcher(eventEndInput, eventEndLayout);
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
     * Configures the adapter and listeners for location input with prediction.
     * The text change listener sends a request to the GeoApify API to retrieve
     * autocomplete predictions. To reduce the number of API calls:
     *
     * <ul>
     * <li>Autocompletion only starts at 3 characters</li>
     * <li>A debounce timer of 300ms is used to reduce API spam</li>
     * <li> The previous GET request in progress is cancelled and replaced should there be a character update</li>
     * </ul>
     */
    private void configureLocationField() {
        locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        locationInput.setAdapter(locationAdapter);
        locationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (currentRequest != null)
                    handler.removeCallbacks(currentRequest);

                if (currentCall != null && !currentCall.isCanceled())
                    currentCall.cancel();

                if (query.isEmpty() || query.length() < 3) {
                    locationAdapter.clear();
                    locationInput.dismissDropDown();
                    return;
                }

                currentRequest = () -> fetchLocationSuggestion(query);
                handler.postDelayed(currentRequest, 300);
            }
        });

        locationInput.setOnItemClickListener((parent, v, position, id) -> {
            String selected = locationAdapter.getItem(position);
            if (selected != null)
                locationInput.setText(selected, false);
        });
    }

    /**
     * Makes an GET request to GeoApify and updates the adapter with the results and triggers the
     * dropdown to show
     *
     * @param query The partial location query from the text field
     */
    private void fetchLocationSuggestion(String query) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.geoapify.com")
                .addPathSegment("v1")
                .addPathSegment("geocode")
                .addPathSegment("autocomplete")
                .addQueryParameter("text", query)
                .addQueryParameter("format", "json")
                .addQueryParameter("apiKey", BuildConfig.GEOAPIFY_API_KEY)
                .build();


        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("LocationDebug", "request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e("LocationDebug", "request unsuccessful");
                    return;
                }

                List<String> newSuggestions = parseSuggestions(body);
                requireActivity().runOnUiThread(() -> {
                    locationAdapter.clear();
                    locationAdapter.addAll(newSuggestions);
                    locationAdapter.notifyDataSetChanged();

                    if (!newSuggestions.isEmpty() && locationInput.hasFocus())
                        locationInput.showDropDown();
                    else
                        locationInput.dismissDropDown();
                });
            }
        });
    }

    /**
     * Reads and parses the JSON response of the GET request and returns the formatted items
     *
     * @param json The JSON string to parse
     * @return Formatted suggestions parsed from the JSOn string
     */
    private List<String> parseSuggestions(String json) {
        List<String> results = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(json);
            JSONArray items = root.optJSONArray("results");

            if (items == null)
                return results;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String formatted = item.optString("formatted");

                if (!formatted.isEmpty())
                    results.add(formatted);
            }
        } catch (JSONException e) {
            Log.e("LocationDebug", "json parse error", e);
        }

        return results;
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
        UIValidationUtil.clearErrors(requiredFields.values());
        createButton.setEnabled(false);

        String name = UIValidationUtil.getText(nameInput);
        String location = UIValidationUtil.getText(locationInput);
        String description = UIValidationUtil.getText(descriptionInput);
        String registrationStart = UIValidationUtil.getText(registrationStartInput);
        String registrationEnd = UIValidationUtil.getText(registrationEndInput);
        String priceText = UIValidationUtil.getText(priceInput);
        String waitlistText = UIValidationUtil.getText(waitlistInput);
        String eventStart = UIValidationUtil.getText(eventStartInput);
        String eventEnd = UIValidationUtil.getText(eventEndInput);


        boolean valid = UIValidationUtil.validateRequiredFields(requiredFields);

        float price = 0.0f;
        if (!priceText.isEmpty()) {
            try {
                price = Float.parseFloat(priceText);
                if (price < 0) {
                    priceLayout.setError("Must be 0 or greater");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                priceLayout.setError("Enter a valid price");
                valid = false;
            }
        }

        int waitlistLimit = -1;
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
        }

        if (!valid)
            return;

        Toast.makeText(requireContext(), "Event created", Toast.LENGTH_SHORT).show();
        EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());
        SessionController controller = SessionController.getInstance(getContext());
        User organizer = controller.getCurrentUser();

        Event.Builder builder = Event.builder()
                .name(name)
                .location(location)
                .description(description)
                .registrationStart(LocalDate.parse(registrationStart, dateFormat).atStartOfDay())
                .registrationEnd(LocalDate.parse(registrationEnd, dateFormat).atStartOfDay())
                .start(LocalDate.parse(eventStart, dateFormat).atStartOfDay())
                .end(LocalDate.parse(eventEnd, dateFormat).atStartOfDay())
                .organizerId(organizer.getId())
                .price(price)
                .waitlistLimit(waitlistLimit);

        Event event = builder.build();
        eventRepository.addEvent(event, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                createButton.setEnabled(true);

                NavHostFragment.findNavController(instance).navigate(R.id.eventListFragment);
                Toast.makeText(getContext(), "Created event successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendFailure(Exception e) {
                createButton.setEnabled(true);
                Toast.makeText(getContext(), "Error creating event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (currentRequest != null)
            handler.removeCallbacks(currentRequest);

        if (currentCall != null && !currentCall.isCanceled())
            currentCall.cancel();
    }
}
