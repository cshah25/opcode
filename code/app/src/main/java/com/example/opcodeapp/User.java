package com.example.opcodeapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class User implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private String email;
    private String phoneNum;
    private List<Event> joinedEvents;
    private List<Event> createdEvents;

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
     * Constructor for the User class (for Parcelable).
     * @param in
     * The Parcel to read from.
     */
    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        phoneNum = in.readString();
        joinedEvents = in.createTypedArrayList(Event.CREATOR);
        createdEvents = in.createTypedArrayList(Event.CREATOR);

    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phoneNum);
    }


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
    public List<Event> getJoinedEvents() {
        return joinedEvents;
    }

    /**
     * Setter for the events the user has joined the waiting list for.
     *
     * @param joinedEvents
     * The events the user has joined.
     */
    public void setJoinedEvents(List<Event> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    /**
     * Getter for the events the user has created/organized.
     *
     * @return
     * The events the user has created/organized.
     */
    public List<Event> getCreatedEvents() {
        return createdEvents;
    }


    /**
     * Setter for the events the user has created/organized.
     *
     * @param createdEvents
     * The events the user has created/organized.
     */
    public void setCreatedEvents(List<Event> createdEvents) {
        this.createdEvents = createdEvents;
    }


    /**
     * Getter for the ID of the user. Filled in by Firestore.
     *
     * @return
     * The ID of the user.
     */
    public String getId() {
        return id;
    }


    /**
     * Setter for the ID of the user. Filled in by Firestore.
     *
     * @param id
     * The ID of the user.
     */
    public void setId(String id) {
        this.id = id;
    }
}
