package com.example.opcodeapp.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.NotificationArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.callback.FirestoreCallbackNotificationsReceive;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.NotificationRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Fragment to display all of a user's notifications
 */
public class NotificationsFragment extends Fragment {
    private NotificationArrayAdapter notifications;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment NotificationsFragment.
     */
    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        User user = SessionController.getInstance(requireContext()).getCurrentUser();
        NotificationRepository repo = new NotificationRepository(FirebaseFirestore.getInstance());

        ListView noti_list_view = view.getRootView().findViewById(R.id.notification_list_view);
        repo.fetchNotificationsByUserId(user.getId(), new FirestoreCallbackNotificationsReceive() {
            @Override
            public void onDataReceived(List<Notification> items) {
                notifications = new NotificationArrayAdapter(requireContext(), items);
                notifications.notifyDataSetChanged();
                noti_list_view.setAdapter(notifications);
                Log.i("NotificationsFragment", "Loaded " + items.size() + " notis");
            }

            @Override
            public void onError(Exception e) {
                Log.e("NotificationsFragment", "Error fetching notis", e);
            }
        });
        noti_list_view.setOnItemClickListener((parent, v, i, id) -> {
            Log.d("NotificationsFragment", "Noti " + i + " clicked");
            NavController controller = NavHostFragment.findNavController(this);
            // don't think noti can be null but I can't confirm
            Notification noti = notifications.getItem(i);
            EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());
            eventRepository.fetchEvent(noti.getEvent_id(), new FirestoreCallbackEventReceive() {
                @Override
                public void onDataReceived(@Nullable Event e) {
                    if (e == null) {
                        Toast.makeText(getContext(), "Associated event doesn't exist", Toast.LENGTH_SHORT).show();
                        Log.e("NotificationsFragment", "Associated event doesn't exist.");
                        return;
                    }
                    // we've got to make sure we navigate to the correct fragment depending if user is the organizer
                    ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
                    applicantRepository.fetchApplicant(user.getId(), e.getId(), new FirestoreCallbackApplicantReceive() {
                        @Override
                        public void onDataReceived(@Nullable Applicant applicant) {
                            Bundle args = new Bundle();
                            args.putParcelable("event", e);
                            if (e.getOrganizerId().equals(user.getId())) {
                                controller.navigate(R.id.organizerEventFragment, args);
                            } else {
                                if (applicant != null) {
                                    args.putParcelable("applicant", applicant);
                                }
                                controller.navigate(R.id.eventDetailsFragment, args);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("NotificationsFragment", "Could not load applicant", e);
                        }
                    });

                }

                @Override
                public void onError(Exception e) {
                    Log.e("NotificationsFragment", "Could not load event", e);
                    Toast.makeText(getActivity(), "Could not load event", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}