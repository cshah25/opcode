package com.example.opcodeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackApplicantReceive;
import com.example.opcodeapp.callback.FirestoreCallbackEventReceive;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Comment;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.example.opcodeapp.repository.CommentRepository;
import com.example.opcodeapp.repository.EventRepository;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {

    public CommentArrayAdapter(Context context, List<Comment> comments) {
        super(context, 0, comments);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.comment_content,
                    parent, false);
        } else {
            view = convertView;
        }



        Comment comment = getItem(position);
        TextView userName = view.findViewById(R.id.user_name);
        TextView content = view.findViewById(R.id.user_comment);
        TextView time = view.findViewById(R.id.time_text);

        TextView role = view.findViewById(R.id.role);
        time.setText(comment.getCommentTime().toString());
        content.setText(comment.getContent());

        EventRepository eventRepository = new EventRepository(FirebaseFirestore.getInstance());

        eventRepository.fetchEvent(comment.getEventId(), new FirestoreCallbackEventReceive() {
            @Override
            public void onDataReceived(Event event) {
                if (event.getOrganizer().getId().equals(comment.getUserId())) {
                    role.setText("Organizer");
                    userName.setText(event.getOrganizer().getName());
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching event", Toast.LENGTH_SHORT).show();
            }


        });

        if (role.getText() != "Organizer") {

            ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());

            applicantRepository.fetchApplicant(comment.getUserId(), comment.getEventId(), new FirestoreCallbackApplicantReceive() {
                @Override
                public void onDataReceived(Applicant applicant) {
                    userName.setText(applicant.getName());
                    role.setText("Applicant");
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error fetching applicant", Toast.LENGTH_SHORT).show();

                }
            });

        }




        return view;
    }

}
