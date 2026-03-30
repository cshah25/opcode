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

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.InvitedUserArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.databinding.FragmentInvitedUsersBinding;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.repository.ApplicantRepository;
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
    private ArrayList<Applicant> dataList;

    /**
     * The ListView for the list of users.
     */
    private ListView userList;

    /**
     * The ArrayAdapter for the list of users.
     */
    private ArrayAdapter<Applicant> userAdapter;
    private ApplicantRepository applicantRepository;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invited_users, container, false);
    }

    //may need to test this
    public List<Applicant> combineLists(Event event) {

        List<Applicant> allInvitedApplicants = new ArrayList<>();
        applicantRepository.fetchApplicantsByEvent(event.getId(), new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                allInvitedApplicants.addAll(applicants);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
            }
        });

        return allInvitedApplicants;
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Event event = getArguments().getParcelable("event");

        applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
        dataList = new ArrayList<>(combineLists(event));
        userList = view.getRootView().findViewById(R.id.invited_users_list_view);
        userAdapter = new InvitedUserArrayAdapter(getContext(), dataList, event);
        userList.setAdapter(userAdapter);


        /**
         * Click Listener for each of the users in the listview. If the user has declined the invitation, they can be removed from the list.
         */
        userList.setOnItemClickListener((parent, view1, position, id) -> {
            Applicant user = userAdapter.getItem(position);

            List<Applicant> declinedApplicants = new ArrayList<>();

            applicantRepository.fetchApplicantsByStatus(event, ApplicantStatus.DECLINED, new FirestoreCallbackApplicantsReceive() {
                        @Override
                        public void onDataReceived(List<Applicant> applicants) {
                            declinedApplicants.addAll(applicants);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "Error fetching applicants", Toast.LENGTH_SHORT).show();
                        }
                    }
            );


            if (declinedApplicants.contains(user)) {
                DeclinedUserDialogFragment declinedUserDialogFragment = DeclinedUserDialogFragment.newInstance(user, event);
                declinedUserDialogFragment.show(getParentFragmentManager(), "Remove");
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     * Removes a user from the list of invited users.
     *
     * @param applicant The user that has declined the invitation.
     * @param event     The event.
     */
    @Override
    public void removeUser(Applicant applicant, Event event) {
        applicant.setStatus(ApplicantStatus.DECLINED_REMOVED);
        applicantRepository.updateApplicant(applicant, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void aVoid) {
                Toast.makeText(getContext(), "User removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendFailure(Exception e) {
                Toast.makeText(getContext(), "Error removing user", Toast.LENGTH_SHORT).show();
            }
        });

        dataList.remove(applicant);
        userAdapter.notifyDataSetChanged();
    }
}
