package com.example.opcodeapp;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class User implements Serializable {

    @DocumentId
    private String id;
    private String name;
    private String email;
    private String phoneNum;
    private Event[] joinedEvents;
    private Event[] createdEvents;

    /**
     * Constructor for the User class.
     * @param name
     * The name of the user.
     * @param email
     * The email of the user.
     * @param phoneNum
     * The phone number of the user.
     */
    public User(String name, String email, String phoneNum) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
    };

    /**
     * Getter for the name of the user.
     *
     * @return
     * The name of the user.
     */
    public String getName() {
        return name;
    };

    /**
     * Setter for the name of the user.
     *
     * @param name
     * The name of the user.
     */
    public void setName(String name) {
        this.name = name;
    };

    /**
     * Getter for the email of the user.
     *
     * @return
     * The email of the user.
     */
    public String getEmail() {
        return email;
    }
    /**
     * Setter for the email of the user.
     *
     * @param email
     * The email of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Getter for the phone number of the user.
     *
     * @return
     * The phone number of the user.
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * Setter for the phone number of the user.
     *
     * @param phoneNum
     * The phone number of the user.
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * Getter for the events the user has joined the waiting list for.
     *
     * @return
     * The events the user has joined.
     */
    public Event[] getJoinedEvents() {
        return joinedEvents;
    }

    /**
     * Setter for the events the user has joined the waiting list for.
     *
     * @param joinedEvents
     * The events the user has joined.
     */
    public void setJoinedEvents(Event[] joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    /**
     * Getter for the events the user has created/organized.
     *
     * @return
     * The events the user has created/organized.
     */
    public Event[] getCreatedEvents() {
        return createdEvents;
    }


    /**
     * Setter for the events the user has created/organized.
     *
     * @param createdEvents
     * The events the user has created/organized.
     */
    public void setCreatedEvents(Event[] createdEvents) {
        this.createdEvents = createdEvents;
    }
}
