package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.UserArrayAdapter;
import com.example.opcodeapp.databinding.FragmentEnrolledUsersBinding;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.User;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * The fragment for the list of users who are enrolled in the event ("Accepted").
 */
public class EnrolledUsersFragment extends Fragment {

    /**
     * The list of users to be displayed.
     */
    private ArrayList<Applicant> dataList;

    /**
     * The ListView for the list of users.
     */
    private ListView userList;

    /**
     * The ArrayAdapter for the list of users.
     */
    private ArrayAdapter<Applicant> userAdapter;


    /**
     * The binding for the fragment.
     */
    private FragmentEnrolledUsersBinding binding;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentUserView = inflater.inflate(R.layout.fragment_enrolled_users, container, false);
        return fragmentUserView;
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        Applicant[] receivedArray = EnrolledUsersFragmentArgs.fromBundle(getArguments()).getUserList();

        dataList = new ArrayList<>(Arrays.asList(receivedArray));

        userList = view.getRootView().findViewById(R.id.enrolled_users_list_view);
        
        userAdapter = new UserArrayAdapter(getContext(), dataList);

        userList.setAdapter(userAdapter);
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
