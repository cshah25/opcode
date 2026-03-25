package com.example.opcodeapp.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;

public class DeclinedUserDialogFragment extends DialogFragment {

    interface DeclinedUserDialogListener {
        void removeUser(Applicant applicant, Event event);
    }

    private DeclinedUserDialogListener listener;

    public static DeclinedUserDialogFragment newInstance(Applicant applicant, Event event) {
        DeclinedUserDialogFragment fragment = new DeclinedUserDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("applicant", applicant);
        args.putParcelable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof DeclinedUserDialogListener) {
            listener = (DeclinedUserDialogListener) getParentFragment();
        } else if (context instanceof DeclinedUserDialogListener) {
            listener = (DeclinedUserDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement DeclinedUserDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_remove_user, null);
        Context context = requireContext();
        Bundle args = getArguments();

        if (args == null) {
            Log.e("MissingBundle", "No arguments set before navigating to this fragment");
            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle("MissingBundle")
                    .setNeutralButton("Close", null)
                    .create();
        }

        Applicant applicant = args.getParcelable("applicant", Applicant.class);
        Event event = args.getParcelable("event", Event.class);
        if (applicant == null || event == null) {
            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle("MissingBundleArguments")
                    .setNeutralButton("Close", null)
                    .create();
        }

        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Remove")
                .setPositiveButton("Remove", (dialog, which) -> listener.removeUser(applicant, event))
                .setNegativeButton("Cancel", null)
                .create();
    }
}
