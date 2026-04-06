package com.example.opcodeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.callback.FirestoreCallbackNotificationReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.databinding.ActivityMainBinding;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.Notification;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.NotificationRepository;
import com.example.opcodeapp.view.RemoveUserDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    // Fragment IDs where the top and bottom navbars are hidden
    private static final List<Integer> hiddenToolbars = List.of(
            R.id.launchFragment,
            R.id.setupFragment
    );

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // this is here to handle noti clicks while the app is open
        String destination = intent.getStringExtra("destination");
        Log.d("MainActivity", "Receiving intent with destination: " + destination);
        if (destination != null) {
            if (destination.equals("details")) {
                handleDetailsIntent(intent);
            } else if (destination.equals("notifications")) {
                handleNotiIntent(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topAppBar);

        NavController navController = getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.launchFragment,
                R.id.setupFragment,
                R.id.eventCreatorFragment,
                R.id.eventListFragment,
                R.id.eventHistoryFragment,
                R.id.qrCodeScannerFragment
        ).build();

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    1
            );
        }

        // configure notification channel
        NotificationChannel channel = new NotificationChannel(
                "default",
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int visibility = (hiddenToolbars.contains(destination.getId())) ? View.GONE : View.VISIBLE;
            binding.bottomNav.setVisibility(visibility);
            binding.topAppBar.setVisibility(visibility);
            binding.topAppBar.setTitle("");
        });

        binding.profileIcon.setOnClickListener(v -> navController.navigate(R.id.profileFragment));

        // handle when the app is started by a notification
        Intent intent = getIntent();
        String destination = intent.getStringExtra("destination");
        if (destination != null) {
            if (destination.equals("details")) {
                handleDetailsIntent(intent);
            } else if (destination.equals("notifications")) {
                handleNotiIntent(intent);
            }
        }
    }

    /**
     * Take the app to the details fragment
     * @param intent: noti intent
     */
    private void handleDetailsIntent(Intent intent) {
        NavController nav = getNavController();

        // we got to find the associated event to pass to event details fragment
        EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());
        String event_id = intent.getStringExtra("event_id");
        User user = SessionController.getInstance(getApplicationContext()).getCurrentUser();
        eventRepository.fetchEvent(event_id, new FirestoreCallbackEventReceive() {
            @Override
            public void onDataReceived(@Nullable Event e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Associated event doesn't exist", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "Associated event doesn't exist.");
                    return;
                }
                ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());
                applicantRepository.fetchApplicant(user.getId(), e.getId(), new FirestoreCallbackApplicantReceive() {
                    @Override
                    public void onDataReceived(@Nullable Applicant applicant) {
                        Bundle args = new Bundle();
                        args.putParcelable("event", e);
                        if (e.getOrganizerId().equals(user.getId())) {
                            Log.w("MainActivity", "Organizer got notification?");
                            nav.navigate(R.id.organizerEventFragment, args);
                        } else {
                            // should always be true unless you click a noti after leaving the
                            // the running for the event without consuming the noti
                            if (applicant != null) {
                                args.putParcelable("applicant", applicant);
                                Log.i("MainActivity", "navigating to details as applicant");
                                nav.navigate(R.id.eventDetailsFragment, args);
                            }
                            Log.i("MainActivity", "navigating to details no applicant");
                            nav.navigate(R.id.eventDetailsFragment, args);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MainActivity", "Could not load applicant", e);
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                Log.e("NotificationsFragment", "Could not load event", e);
                Toast.makeText(getApplicationContext(), "Could not load event", Toast.LENGTH_SHORT).show();
            }
        });

        // set notification as read since we saw it
        String noti_id = intent.getStringExtra("noti_id");
        NotificationRepository repo = new NotificationRepository(FirebaseFirestore.getInstance());
        repo.fetchNotification(noti_id, new FirestoreCallbackNotificationReceive() {
            @Override
            public void onDataReceived(Notification item) {
                item.setRead(true);
                repo.updateNotification(item, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {
                        Log.i("MainActivity", "noti marked as read");
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Log.e("MainActivity", "couldn't mark noti as read");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Couldn't find notification");
            }
        });
    }

    /**
     * Take the app to the notifications page
     * @param intent: noti intent
     */
    private void handleNotiIntent(Intent intent) {
        NavController nav = getNavController();

        // set notification as read since we saw it
        String noti_id = intent.getStringExtra("noti_id");
        NotificationRepository repo = new NotificationRepository(FirebaseFirestore.getInstance());
        repo.fetchNotification(noti_id, new FirestoreCallbackNotificationReceive() {
            @Override
            public void onDataReceived(Notification item) {
                item.setRead(true);
                repo.updateNotification(item, new FirestoreCallbackSend() {
                    @Override
                    public void onSendSuccess(Void unused) {
                        Log.i("MainActivity", "noti marked as read");
                    }

                    @Override
                    public void onSendFailure(Exception e) {
                        Log.e("MainActivity", "couldn't mark noti as read");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("MainActivity", "Couldn't find notification");
            }
        });

        nav.navigate(R.id.notificationsFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private NavController getNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return (navHostFragment != null)
                ? navHostFragment.getNavController()
                : Navigation.findNavController(this, R.id.nav_host_fragment);
    }
}
