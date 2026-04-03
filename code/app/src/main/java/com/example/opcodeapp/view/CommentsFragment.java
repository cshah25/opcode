package com.example.opcodeapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.R;
import com.example.opcodeapp.adapter.CommentArrayAdapter;
import com.example.opcodeapp.callback.FirestoreCallbackCommentsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.controller.SessionController;
import com.example.opcodeapp.model.Comment;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.CommentRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * The fragment for the list of users who are enrolled in the event ("Accepted").
 */
public class CommentsFragment extends Fragment {

    private EditText commentInput;
    private ArrayAdapter<Comment> commentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        Event event = args.getParcelable("event", Event.class);
        if (event == null) {
            return;
        }

        ListView commentListView = view.getRootView().findViewById(R.id.comments_list_view);
        commentAdapter = new CommentArrayAdapter(requireContext(), getComments(event), event);
        commentListView.setAdapter(commentAdapter);
        commentAdapter.notifyDataSetChanged();

        View comment_controls = view.findViewById(R.id.comment_controls);
        Button add_comment = view.findViewById(R.id.btn_add_comment);
        commentInput = view.findViewById(R.id.comment_input);

        add_comment.setOnClickListener(v -> {
            addComment(event);
            commentAdapter.notifyDataSetChanged();
            commentInput.setText("");
        });
    }

    private void addComment(Event event) {

        String input = commentInput.getText().toString();

        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a comment!", Toast.LENGTH_SHORT).show();
            return;
        }


        CommentRepository repository = new CommentRepository(FirebaseFirestore.getInstance());
        SessionController sessionController = SessionController.getInstance(requireContext());
        User curr_user = sessionController.getCurrentUser();
        Comment comment = Comment.builder()
                .eventId(event.getId())
                .userId(curr_user.getId())
                .content(input)
                .time(LocalDateTime.now())
                .build();

        repository.addComment(comment, new FirestoreCallbackSend() {
            @Override
            public void onSendSuccess(Void unused) {
                Log.d("AddComment", "Comment added successfully");
            }

            @Override
            public void onSendFailure(Exception e) {
                Log.e("AddComment", "Failed to add comment: " + e.getMessage());

            }

        });


    }


    @NonNull
    private static List<Comment> getComments(Event event) {
        List<Comment> data_list = new ArrayList<>();
        CommentRepository repository = new CommentRepository(FirebaseFirestore.getInstance());
        repository.fetchCommentsByEvent(event.getId(), new FirestoreCallbackCommentsReceive() {
                    @Override
                    public void onDataReceived(List<Comment> comments) {
                        data_list.addAll(comments);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("FetchApplicantError", "An error occurred: " + e.getMessage());
                    }
                }
        );
        return data_list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
