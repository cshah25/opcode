package com.example.opcodeapp;

import androidx.fragment.app.DialogFragment;

public class DeclinedUserDialogFragment extends DialogFragment {

    interface DeclinedUserDialogListener {
        void removeUser(User user, Event event);


    }
}
