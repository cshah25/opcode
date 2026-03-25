package com.example.opcodeapp;

import static java.security.AccessController.getContext;

import android.util.Log;
import android.widget.Toast;

import com.example.opcodeapp.callback.FirestoreCallbackApplicantsReceive;
import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.model.Applicant;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.example.opcodeapp.repository.ApplicantRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the selection of entrants from an event's waiting list.
 */
public class LotterySystem {

    private ApplicantRepository applicantRepository = new ApplicantRepository(FirebaseFirestore.getInstance());


    /**
     * Randomly selects a specified number of entrants from the waitlist.
     * Ensures capacity is not exceeded.
     * * @param event The event containing the waitlist.
     * @param numberOfInvites The number of people to draw.
     * @return List of Users selected to receive an invitation.
     */
    public List<Applicant> drawEntrants(Event event, int numberOfInvites) {

        List<Applicant> waitlist = new ArrayList<>();




        applicantRepository.fetchApplicantsByStatus(event, ApplicantStatus.NOT_DRAWN, new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                waitlist.addAll(applicants);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Lottery", "Error fetching applicants. Possibly no applicants in the waiting list", e);

            }

        });

         //change made here

        // Responsibility: don't exceed capacity or available applicants
        int drawSize = Math.min(numberOfInvites, waitlist.size());

        // Responsibility: randomly assign entrants
        Collections.shuffle(waitlist);

        return waitlist.subList(0, drawSize);
    }

    /**
     * Draws a single replacement if an invitation is declined.
     * @param event The event.
     * @return A single randomly selected User from the remaining waitlist.
     */
    public Applicant drawReplacement(Event event) {





        List<Applicant> waitlist_applicants = new ArrayList<>();

        applicantRepository.fetchApplicantsByStatus(event, ApplicantStatus.NOT_DRAWN, new FirestoreCallbackApplicantsReceive() {
            @Override
            public void onDataReceived(List<Applicant> applicants) {
                waitlist_applicants.addAll(applicants);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Lottery", "Error fetching applicants. Possibly no applicants in the waiting list", e);

            }
        });


        if (waitlist_applicants.isEmpty()) {
            return null;
        } else {
            return waitlist_applicants.get(0);
        }

    }
}