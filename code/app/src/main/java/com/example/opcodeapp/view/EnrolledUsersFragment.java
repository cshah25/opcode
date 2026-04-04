package com.example.opcodeapp.view;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.ApplicantArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;


import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The fragment for the list of users who are enrolled in the event ("Accepted").
 */
public class EnrolledUsersFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enrolled_users, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {

            return;
        }


        Event event = args.getParcelable("event", Event.class);
        if (event == null) {

            return;
        }

        ImageButton downloadButton = view.getRootView().findViewById(R.id.btn_download_csv);




        ListView applicantListView = view.getRootView().findViewById(R.id.enrolled_users_list_view);
        List<Applicant> dataList = new ArrayList<>();
        ApplicantRepository repository = new ApplicantRepository(FirebaseFirestore.getInstance());
        repository.fetchApplicants(f -> f.whereEqualTo("status", ApplicantStatus.ACCEPTED.name()),
                new FirestoreCallbackApplicantsReceive() {

                    /**
                     * updates the adapter and displayes all the enrolled applicants. Additionally, it activates the download button.
                     * @param applicants
                     * list of applicants to display.
                     */
                    @Override
                    public void onDataReceived(List<Applicant> applicants) {
                        dataList.addAll(applicants);
                        ArrayAdapter<Applicant> applicantAdapter = new ApplicantArrayAdapter(requireContext(), dataList);
                        applicantListView.setAdapter(applicantAdapter);
                        applicantAdapter.notifyDataSetChanged();

                        /**
                         * Converts the list of applicants to a CSV file and saves it to the downloads folder.
                         */
                        downloadButton.setOnClickListener(v -> {
                            convertToCSV(applicants);
                        });
                    }

                    /**
                     * handles errors.
                     * @param e
                     * exception to handle.
                     */
                    @Override
                    public void onError(Exception e) {
                        Log.e("FetchApplicantError", "An error occurred: " + e.getMessage());
                    }
                }
        );

    }

    /**
     * Converts the list of applicants to a CSV file by obtaining the applicant information from the user repo.
     * @param applicants
     * list of applicants to convert to CSV.
     */
    public void convertToCSV(List<Applicant> applicants) {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"Name", "Email", "Phone Number"});

        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
        final int total = applicants.size();
        final AtomicInteger counter = new AtomicInteger(0);

        for (Applicant applicant : applicants) {
            userRepository.fetchUser(applicant.getUserId(), new FirestoreCallbackUserReceive() {
                /**
                 * receives the user information and adds it to the data list.
                 * @param user
                 * the user to add to the data list.
                 */
                @Override
                public void onDataReceived(User user) {
                    data.add(new String[]{user.getName(), user.getEmail(), user.getPhoneNum()});

                    // Check if this was the last user to be fetched
                    if (counter.incrementAndGet() == total) {
                        saveFileToDisk(data); // Call a separate helper to write the file
                    }
                }

                /**
                 * if the user is not found then they are skipped.
                 * @param e
                 * exception to handle.
                 */
                @Override
                public void onError(Exception e) {
                    if (counter.incrementAndGet() == total) {
                        saveFileToDisk(data);
                    }
                }
            });
        }
    }


    /**
     * takes the user data in csv format and saves it to the downloads folder.
     * @param data
     * Each row in the array represents a user with their name, email, and phone number.
     */
    private void saveFileToDisk(List<String[]> data) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "enrolled_users.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeAll(data);
            Log.d("CSV", "File saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
