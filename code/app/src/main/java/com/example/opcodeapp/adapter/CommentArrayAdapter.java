package com.example.opcodeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.opcodeapp.R;
import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.model.Comment;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    private final Event event;

    public CommentArrayAdapter(Context context, List<Comment> comments, Event event) {
        super(context, 0, comments);
        this.event = event;
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

        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance());

        Comment comment = getItem(position);
        TextView userName = view.findViewById(R.id.user_name);
        TextView content = view.findViewById(R.id.user_comment);
        TextView time = view.findViewById(R.id.time_text);

        content.setText(comment.getContent());
        String formattedTime = comment.getFormattedTime();
        time.setText(formattedTime);

        userRepository.fetchUser(comment.getUserId(), new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                String organizerId = event.getOrganizerId();
                String commenterId = comment.getUserId();
                userName.setText(organizerId.equals(commenterId) ? user.getName() + "(Organizer)" : user.getName());
            }

            @Override
            public void onError(Exception e) {
                userName.setText("[User]");
            }
        });

        return view;
    }
}
