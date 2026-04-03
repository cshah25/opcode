package com.example.opcodeapp.view;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.List;


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

        List<Applicant> dataList = getApplicants();
        ArrayAdapter<Applicant> applicantAdapter = new ApplicantArrayAdapter(requireContext(), dataList);
        ListView applicantListView = view.getRootView().findViewById(R.id.enrolled_users_list_view);
        applicantListView.setAdapter(applicantAdapter);
        applicantAdapter.notifyDataSetChanged();
    }

    @NonNull
    private static List<Applicant> getApplicants() {
        List<Applicant> dataList = new ArrayList<>();
        ApplicantRepository repository = new ApplicantRepository(FirebaseFirestore.getInstance());
        repository.fetchApplicants(f -> f.whereEqualTo("status", ApplicantStatus.ACCEPTED.name()),
                new FirestoreCallbackApplicantsReceive() {
                    @Override
                    public void onDataReceived(List<Applicant> applicant) {
                        dataList.addAll(applicant);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("FetchApplicantError", "An error occurred: " + e.getMessage());
                    }
                }
        );
        return dataList;
    }

    public void convertToCSV(List<Applicant> applicants) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "enrolled_users.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {

            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"Name", "Email", "Phone Number"});

            UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());
            for (Applicant applicant : applicants) {
                userRepository.fetchUser(applicant.getUserId(), new FirestoreCallbackUserReceive() {
                    @Override
                    public void onDataReceived(User user) {
                        data.add(new String[]{user.getName(), user.getEmail(), user.getPhoneNum()});
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("FetchUserError", "An error occurred: " + e.getMessage());

                    }
                });

            }

            writer.writeAll(data);
        } catch (Exception e) {
            e.printStackTrace();


        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
