package com.example.opcodeapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.enums.LoginState;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LaunchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LaunchFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static LaunchFragment newInstance() {
        LaunchFragment fragment = new LaunchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_launch, container, false);

        NavController nav = NavHostFragment.findNavController(this);
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.launchFragment, true)
                .build();

        SessionController.getInstance(getContext()).getLoginState()
                .observe(getViewLifecycleOwner(), state -> {
                    if (state == LoginState.LOGGED_IN) {
                        nav.navigate(R.id.main_graph, null, options);
                    } else if (state == LoginState.LOGGED_OUT) {
                        nav.navigate(R.id.setup_graph, null, options);
                    }
                });
        return v;
    }
}