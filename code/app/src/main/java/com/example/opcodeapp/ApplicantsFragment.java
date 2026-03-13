package com.example.opcodeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.databinding.FragmentEnrolledUsersBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The fragment for the list of users who applied for the event.
 */
public class ApplicantsFragment extends Fragment {


    /**
     * The list of users to be displayed.
     */
    private ArrayList<User> dataList;

    /**
     * The ListView for the list of users.
     */
    private ListView userList;

    /**
     * The ArrayAdapter for the list of users.
     */
    private ArrayAdapter<User> userAdapter;


    /**
     * The binding for the fragment.
     */
    private FragmentEnrolledUsersBinding binding;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentUserView = inflater.inflate(R.layout.fragment_applicants, container, false);
        return fragmentUserView;
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Event event = getArguments().getParcelable("event");

        List<User> Attendees = event.getAttendees();

        List<User> applicants = event.getInitialApplicants();

        List<User> invited = event.getInvited();

        List<User> declined = event.getDeclined();

        List<User> declined_removed = event.getDeclinedRemoved();

        List<User> all_applicants = new ArrayList<>();
        all_applicants.addAll(Attendees);
        all_applicants.addAll(applicants);
        all_applicants.addAll(invited);
        all_applicants.addAll(declined);
        all_applicants.addAll(declined_removed);


        User[] receivedArray = all_applicants.toArray(new User[0]);






        dataList = new ArrayList<>(Arrays.asList(receivedArray));

        userList = view.getRootView().findViewById(R.id.applicant_users_list_view);

        userAdapter = new EnrolledUserArrayAdapter(getContext(), dataList);

        userList.setAdapter(userAdapter);




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
