package com.example.opcodeapp.view;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.BuildConfig;
import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.util.DateUtil;
import com.example.opcodeapp.util.ValidationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private TextInputLayout nameLayout;
    private TextInputLayout locationLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout registrationPeriodLayout;
    private TextInputLayout eventPeriodLayout;
    private TextInputLayout priceLayout;
    private TextInputLayout waitlistLayout;

    private TextInputEditText nameInput;
    private MaterialAutoCompleteTextView locationInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText registrationPeriodInput;
    private TextInputEditText priceInput;
    private TextInputEditText waitlistInput;
    private TextInputEditText eventPeriodInput;
    private Map<EditText, TextInputLayout> requiredFields;

    private MaterialSwitch publicSwitch;
    private MaterialButton createButton;
    private MaterialButton uploadButton;

    private final DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);
    private LocalDateTime registrationStart, registrationEnd, startDate, endDate;

    private String encodedPoster = "";
    private final ActivityResultLauncher<String> posterPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null)
                    handlePosterUpload(uri);
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
        createButton = view.findViewById(R.id.event_creator_submit_btn);
        uploadButton = view.findViewById(R.id.event_creator_upload_btn);
        bindViews(view);
        addErrorClearingWatchers();



        // Setup picker for registration period
        registrationPeriodLayout.setEndIconOnClickListener(v -> openRegistrationSelector());
        registrationPeriodInput.setOnClickListener(v -> openRegistrationSelector());
        registrationPeriodInput.setKeyListener(null);

        // Setup picker for event period
        eventPeriodLayout.setEndIconOnClickListener(v -> openEventPeriodSelector());
        eventPeriodInput.setOnClickListener(v -> openEventPeriodSelector());
        eventPeriodInput.setKeyListener(null);
        setEventPeriodEnabled(false);

        configureLocationField();

        updatePosterButton();
        uploadButton.setOnClickListener(v -> {
            if (encodedPoster.isEmpty())
                posterPickerLauncher.launch("image/*");
            else {
                encodedPoster = "";
                updatePosterButton();
            }
        });
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
        registrationPeriodLayout = view.findViewById(R.id.event_creator_registration_period_layout);
        priceLayout = view.findViewById(R.id.event_creator_price_layout);
        waitlistLayout = view.findViewById(R.id.event_creator_waitlist_layout);
        eventPeriodLayout = view.findViewById(R.id.event_creator_period_layout);

        nameInput = view.findViewById(R.id.event_creator_name_input);
        locationInput = view.findViewById(R.id.event_creator_location_input);
        descriptionInput = view.findViewById(R.id.event_creator_description_input);
        registrationPeriodInput = view.findViewById(R.id.event_creator_registration_period_input);
        priceInput = view.findViewById(R.id.event_creator_price_input);
        waitlistInput = view.findViewById(R.id.event_creator_waitlist_input);
        eventPeriodInput = view.findViewById(R.id.event_creator_period_input);

        requiredFields = Map.of(
                nameInput, nameLayout,
                locationInput, locationLayout,
                descriptionInput, descriptionLayout,
                registrationPeriodInput, registrationPeriodLayout,
                eventPeriodInput, eventPeriodLayout
        );

        publicSwitch = view.findViewById(R.id.event_creator_public_switch);
    }

    /**
     * Attaches watchers to all fields to clear errors when updated
     */
    private void addErrorClearingWatchers() {
        ValidationUtil.addErrorClearingWatcher(nameInput, nameLayout);
        ValidationUtil.addErrorClearingWatcher(locationInput, locationLayout);
        ValidationUtil.addErrorClearingWatcher(descriptionInput, descriptionLayout);
        ValidationUtil.addErrorClearingWatcher(registrationPeriodInput, registrationPeriodLayout);
        ValidationUtil.addErrorClearingWatcher(priceInput, priceLayout);
        ValidationUtil.addErrorClearingWatcher(waitlistInput, waitlistLayout);
        ValidationUtil.addErrorClearingWatcher(eventPeriodInput, eventPeriodLayout);
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
     * On-click handler that opens the registration period's date picker. This will exclude any
     * dates before the current date.
     */
    private void openRegistrationSelector() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(today))
                .build();

        MaterialDatePicker<Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select registration period")
                        .setCalendarConstraints(constraints)
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            registrationStart = DateUtil.fromMillis(selection.first);
            registrationEnd = DateUtil.fromMillis(selection.second);

            String output = registrationStart.format(dateFormat) + " - " + registrationEnd.format(dateFormat);
            registrationPeriodInput.setText(output);

            eventPeriodInput.setText(null);
            startDate = null;
            endDate = null;
            setEventPeriodEnabled(true);
        });

        picker.show(getParentFragmentManager(), "registration_range_picker");
    }

    private void setEventPeriodEnabled(boolean enabled) {
        eventPeriodInput.setEnabled(enabled);
        eventPeriodLayout.setEnabled(enabled);
    }

    /**
     * On-click handler that opens the event period's date picker. This excludes all dates before
     * today and any dates before the registration period
     */
    private void openEventPeriodSelector() {
        if (registrationStart == null || registrationEnd == null) {
            Toast.makeText(requireContext(), "Select registration period first", Toast.LENGTH_SHORT).show();
            return;
        }

        long minEventDate = registrationEnd
                .plusDays(1)
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minEventDate))
                .build();

        MaterialDatePicker<Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select event period")
                        .setCalendarConstraints(constraints)
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) {
                return;
            }

            startDate = DateUtil.fromMillis(selection.first);
            endDate = DateUtil.fromMillis(selection.second);

            String output = startDate.format(dateFormat) + " - " + endDate.format(dateFormat);
            eventPeriodInput.setText(output);
        });

        picker.show(getParentFragmentManager(), "event_range_picker");
    }

    /**
     * Compresses the provided image poster using the JPEG compression format and
     * encodes the compressed image to a Base64 String for storing
     *
     * @param uri The URI of the image uploaded
     */
    private void handlePosterUpload(Uri uri) {
        try {
            ContentResolver resolver = requireContext().getContentResolver();
            ImageDecoder.Source source = ImageDecoder.createSource(resolver, uri);
            Bitmap bitmap = ImageDecoder.decodeBitmap(source);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            byte[] bytes = out.toByteArray();
            encodedPoster = Base64.encodeToString(bytes, Base64.DEFAULT);

            updatePosterButton();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the upload poster button based on if there is an existing uploaded poster
     */
    private void updatePosterButton() {
        uploadButton.setText(encodedPoster.isEmpty() ? "Upload Poster" : "Cancel");
        uploadButton.setIconResource(encodedPoster.isEmpty() ? R.drawable.ic_upload_icon : R.drawable.ic_close_icon);
    }

    /**
     * Handles validation of input parameters before creating new Event. It returns errors in the ui
     * for any failing validation tests. It then saves to the Firestore database and navigates
     * forward to the event details screen.
     */
    private void submitForm() {
        ValidationUtil.clearErrors(requiredFields.values());
        createButton.setEnabled(false);

        String name = ValidationUtil.getText(nameInput);
        String location = ValidationUtil.getText(locationInput);
        String description = ValidationUtil.getText(descriptionInput);
        String priceText = ValidationUtil.getText(priceInput);
        String waitlistText = ValidationUtil.getText(waitlistInput);

        boolean valid = ValidationUtil.validateRequiredFields(requiredFields);

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

        if (!valid) {
            createButton.setEnabled(true);
            return;
        }

        if (registrationStart == null || registrationEnd == null || startDate == null || endDate == null) {
            createButton.setEnabled(true);
            return;
        }

        if (startDate.isBefore(registrationEnd.plusDays(1))) {
            eventPeriodLayout.setError("Event must start after registration ends");
            createButton.setEnabled(true);
            return;
        }

        EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());
        SessionController controller = SessionController.getInstance(getContext());
        User organizer = controller.getCurrentUser();

        Event event = Event.builder()
                .name(name)
                .location(location)
                .description(description)
                .registrationStart(registrationStart)
                .registrationEnd(registrationEnd)
                .start(startDate)
                .end(endDate)
                .organizerId(organizer.getId())
                .price(price)
                .waitlistLimit(waitlistLimit)
                .waitlistCount(0)
                .isPublic(publicSwitch.isChecked())
                .encodedImage(encodedPoster)
                .build();

        eventRepository.addEvent(event, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                createButton.setEnabled(true);

                NavHostFragment.findNavController(EventCreatorFragment.this).navigate(R.id.eventListFragment);
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
