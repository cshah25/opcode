package com.example.opcodeapp;

import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;

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
     * @param numberOfInvites The number of people to draw.
     * @return List of Users selected to receive an invitation.
     */
    public List<User> drawEntrants(Event event, int numberOfInvites) {
        List<User> applicants = event.getInitialApplicants(); //change made here since getApplicants returns a list (not an array)
        if (applicants == null || applicants.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> waitlist = new ArrayList<>(applicants); //change made here

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
    public User drawReplacement(Event event) {
        List<User> applicants = drawEntrants(event, 1);
        if (applicants.isEmpty())
            return null;
        else
            return applicants.get(0);
    }
}