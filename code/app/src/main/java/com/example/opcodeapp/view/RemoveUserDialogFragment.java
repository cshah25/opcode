package com.example.opcodeapp.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;

public class RemoveUserDialogFragment extends DialogFragment {


    interface RemoveUserDialogListener {
        void removeUser(User user);
    }

    private RemoveUserDialogFragment.RemoveUserDialogListener listener;

    public static RemoveUserDialogFragment newInstance(User user) {
        RemoveUserDialogFragment fragment = new RemoveUserDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("User", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof RemoveUserDialogFragment.RemoveUserDialogListener) {
            listener = (RemoveUserDialogFragment.RemoveUserDialogListener) getParentFragment();
        } else if (context instanceof RemoveUserDialogFragment.RemoveUserDialogListener) {
            listener = (RemoveUserDialogFragment.RemoveUserDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement RemoveUserDialogListener");
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

        User user = args.getParcelable("user", User.class);
        if (user == null) {
            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle("MissingBundleArguments")
                    .setNeutralButton("Close", null)
                    .create();
        }

        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Remove User")
                .setPositiveButton("Remove", (dialog, which) -> listener.removeUser(user))
                .setNegativeButton("Cancel", null)
                .create();
    }
}
