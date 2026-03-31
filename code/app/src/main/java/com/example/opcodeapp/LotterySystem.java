package com.example.opcodeapp;

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

    /**
     * Randomly selects a specified number of entrants from the waitlist.
     * Ensures capacity is not exceeded.
     * * @param event The event containing the waitlist.
     *
     * @param numberOfInvites The number of people to draw.
     * @return List of Users selected to receive an invitation.
     */
    public List<Applicant> drawEntrants(Event event, int numberOfInvites) {
        List<Applicant> result = new ArrayList<>();

        // Responsibility: don't exceed capacity or available applicants
        int waitlistLimit = event.getWaitlistLimit();
        int drawSize = (waitlistLimit != -1) ? numberOfInvites : waitlistLimit;

        ApplicantRepository repository = new ApplicantRepository(FirebaseFirestore.getInstance());
        repository.fetchApplicantsByStatus(event, ApplicantStatus.NOT_DRAWN,
                new FirestoreCallbackApplicantsReceive() {
                    @Override
                    public void onDataReceived(List<Applicant> applicant) {
                        result.addAll(applicant);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                }
        );

        // Responsibility: randomly assign entrants
        Collections.shuffle(result);
        return result.subList(0, drawSize);
    }

    /**
     * Draws a single replacement if an invitation is declined.
     *
     * @param event The event.
     * @return A single randomly selected User from the remaining waitlist.
     */
    public Applicant drawReplacement(Event event) {
        List<Applicant> applicants = drawEntrants(event, 1);
        return !applicants.isEmpty() ? applicants.getFirst() : null;
    }
}