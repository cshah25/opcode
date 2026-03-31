package com.example.opcodeapp.callback;

import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Comment;

import java.util.List;

public interface FirestoreCallbackCommentsReceive {


    void onDataReceived(List<Comment> comments);
    void onError(Exception e);


}
