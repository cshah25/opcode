package com.example.opcodeapp.view;

import android.os.Bundle;
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
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.google.firebase.firestore.FirebaseFirestore;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
