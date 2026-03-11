package com.example.opcodeapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class DeclinedUserDialogFragment extends DialogFragment {

    interface DeclinedUserDialogListener {
        void removeUser(User user, Event event);

        //void drawUser(Event event);



    }

    private DeclinedUserDialogListener listener;

    public static DeclinedUserDialogFragment newInstance(User user, Event event) {
        Bundle args = new Bundle();
        args.putParcelable("User", user);
        args.putParcelable("Event", event);

        DeclinedUserDialogFragment fragment = new DeclinedUserDialogFragment();
        fragment.setArguments(args);
        return fragment;


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeclinedUserDialogListener) {
            listener = (DeclinedUserDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DeclinedUserDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_remove_user, null);


        String tag = getTag();

        Bundle bundle = getArguments();
        User user;
        Event event;

        if (Objects.equals(tag, "Remove") && bundle != null) {
            user = (User) bundle.getParcelable("User");
            event = (Event) bundle.getParcelable("Event");
            assert user != null;
            assert event != null;

        } else {
            user = null;
            event = null;

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Remove")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {

                    if (Objects.equals(tag, "Remove")) {
                        listener.removeUser(user, event);
                    }
                }).create();



    }




}
