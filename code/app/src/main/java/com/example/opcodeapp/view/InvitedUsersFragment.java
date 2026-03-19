package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackSend;
import com.example.opcodeapp.adapter.InvitedUserArrayAdapter;
import com.example.opcodeapp.R;
import com.example.opcodeapp.databinding.FragmentInvitedUsersBinding;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;


/**
 * The fragment for the list of users who applied for the event and were invited.
 */
public class InvitedUsersFragment extends Fragment implements DeclinedUserDialogFragment.DeclinedUserDialogListener {

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
        Event event = getArguments().getParcelable("event");







        dataList = new ArrayList<>(combineLists(event));

        userList = view.getRootView().findViewById(R.id.invited_users_list_view);

        userAdapter = new InvitedUserArrayAdapter(getContext(), dataList, event);

        userList.setAdapter(userAdapter);


        /**
         * Click Listener for each of the users in the listview. If the user has declined the invitation, they can be removed from the list.
         */
        userList.setOnItemClickListener((parent, view1, position, id) -> {
            User user = userAdapter.getItem(position);


            if (event.getDeclined().contains(user)) {
                DeclinedUserDialogFragment declinedUserDialogFragment = DeclinedUserDialogFragment.newInstance(user, event);
                declinedUserDialogFragment.show(getParentFragmentManager(), "Remove");


            }

;

        });




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * Removes a user from the list of invited users.
     *
     * @param user
     * The user that has declined the invitation.
     * @param event
     * The event.
     */
    @Override
    public void removeUser(User user, Event event) {

        FirebaseFirestore DB = FirebaseFirestore.getInstance();
        DBManager dbmanager = new DBManager(DB);
        event.setDeclinedRemoved(user);
        dbmanager.updateEvent(event, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess() {
                userAdapter.notifyDataSetChanged();

            }

            @Override
            public void onSendFailure(Exception e) {
                Toast toast = Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG);

                toast.show();
            }
        });





    }
}
