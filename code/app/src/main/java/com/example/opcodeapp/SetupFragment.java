package com.example.opcodeapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment {

    private EditText name;
    private EditText email;
    private EditText phone;

    public SetupFragment() {
        // Required empty public constructor
    }

    /**
     * Create the login fragment
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static SetupFragment newInstance() {
        return new SetupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        name = view.findViewById(R.id.setup_name_input);
        email = view.findViewById(R.id.setup_email_input);
        phone = view.findViewById(R.id.setup_phone_input);

        Button create = view.findViewById(R.id.setup_create_button);
        SetupFragment not_this = this;

        create.setOnClickListener(v -> {
            String name_t = name.getText().toString();
            String email_t = email.getText().toString();
            String phone_t = phone.getText().toString();

            if (name_t.isEmpty() || email_t.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in Name and Email", Toast.LENGTH_SHORT).show();
            } else {
                DBManager db = new DBManager(FirebaseFirestore.getInstance());
                User user = new User(name_t, email_t, phone_t, getContext());

                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(user.getEmail(), user.getDeviceId())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    SessionController.getInstance(getContext()).updateFireAuth();
                                    db.addUser(user, new FirestoreCallbackSend() {
                                        @Override
                                        public void onSendSuccess() {
                                            Log.i("Setup", "account created");
                                            Toast.makeText(getContext(), "Account successfully created", Toast.LENGTH_SHORT).show();

                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("user", user);

                                            NavController nav = NavHostFragment.findNavController(not_this);
                                            nav.navigate(R.id.action_setupFragment_to_main_graph, bundle);
                                        }

                                        @Override
                                        public void onSendFailure(Exception e) {
                                            Log.e("Setup", String.format("error creating account: %s", e));
                                            Toast.makeText(getContext(), String.format("Error: %s", e), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Log.e("Setup", "Couldn't login with fire");
                                }
                            }
                        });
            }
        });
        return view;
    }
}