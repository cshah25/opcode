package com.example.opcodeapp.repository;

import androidx.annotation.Nullable;

import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackCommentsReceive;
import com.example.opcodeapp.callback.FirestoreCallbackSend;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Comment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CommentRepository extends Repository {

    public CommentRepository(FirebaseFirestore db) {
        super(db, "Comments");
    }

    public void addComment(Comment comment, FirestoreCallbackSend listener) {
        DocumentReference newDocRef = ref.document();
        comment.setId(newDocRef.getId());
        newDocRef.set(comment.toMap())
                .addOnSuccessListener(listener::onSendSuccess)
                .addOnFailureListener(listener::onSendFailure);
    }

    public void fetchComments(@Nullable Function<Query, Query> filter, FirestoreCallbackCommentsReceive listener) {
        Query query = (filter == null) ? ref : filter.apply(ref);
        query.orderBy("comment_time", Query.Direction.DESCENDING).get().addOnSuccessListener(snapshot -> {
            List<Comment> items = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot) {
                Comment comment = Comment.fromMap(document.getId(), document.getData());
                items.add(comment);
            }
            listener.onDataReceived(items);
        }).addOnFailureListener(listener::onError);
    }

    public void fetchCommentsByEvent(String eventId, FirestoreCallbackCommentsReceive listener) {

        fetchComments(q -> q.whereEqualTo("event_id", eventId), listener);

    }



}
