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
import com.example.opcodeapp.databinding.FragmentInvitedUsersBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvitedUsersFragment extends Fragment {

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
    private FragmentInvitedUsersBinding binding;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentUserView = inflater.inflate(R.layout.fragment_invited_users, container, false);
        return fragmentUserView;
    }

    //may need to test this
    public List<User> combineLists(Event event) {
        List<User> allInvitedUsers = new ArrayList<>(event.getInvited());
        allInvitedUsers.addAll(event.getAttendees());
        allInvitedUsers.addAll(event.getDeclined());

        return allInvitedUsers;


    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Event event = InvitedUsersFragmentArgs.fromBundle(getArguments()).getEvent();






        dataList = new ArrayList<>(combineLists(event));

        userList = view.getRootView().findViewById(R.id.invited_users_list_view);

        userAdapter = new InvitedUserArrayAdapter(getContext(), dataList, event);

        userList.setAdapter(userAdapter);


        userList.setOnItemClickListener((parent, view1, position, id) -> {
            User user = userAdapter.getItem(position);

            /*
            if (event.getDeclined().contains(user)) {

            })
            */
;

        }




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
