package com.example.opcodeapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.opcodeapp.R;

import com.example.opcodeapp.adapter.CommentArrayAdapter;

import com.example.opcodeapp.callback.FirestoreCallbackCommentsReceive;

import com.example.opcodeapp.model.Comment;
import com.example.opcodeapp.model.Event;

import com.example.opcodeapp.repository.CommentRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


/**
 * The fragment for the list of users who are enrolled in the event ("Accepted").
 */
public class CommentsFragment extends Fragment {

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

        List<Comment> dataList = getComments(event);
        ArrayAdapter<Comment> commentAdapter = new CommentArrayAdapter(requireContext(), dataList);
        ListView commentListView = view.getRootView().findViewById(R.id.comments_list_view);
        commentListView.setAdapter(commentAdapter);
        commentAdapter.notifyDataSetChanged();
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
