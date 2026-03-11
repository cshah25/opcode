package com.example.opcodeapp;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.DocumentId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.firebase.firestore.DocumentId;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;


@SuppressWarnings("deprecated")

/**
 * The Event class.
 * Contains all the information about an event.
 */
public class Event implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private String location;
    private String description;
    private String startDate;
    private String endDate;
    private String registration_startTime;
    private String registration_endTime;
    private float price;

    private User organizer;

    /**
     * A map containing all the applicants (instances of User class) of the event as keys.
     * The corresponding value of the key is the status of the applicant (i.e. "Not Drawn", "Invited", "Accepted", "Declined", "Declined-Removed").
     *
     */
    private Map<User, String> applicants = new HashMap<>();

    /**
     * Needed for toObject from Firebase
     */
    public Event() {}

    /**
     * Constructor for the Event class.
     * @param name
     * The name of the event.
     * @param location
     * The location of the event.
     * @param description
     * The description of the event.
     * @param startDate
     * The start date of the event.
     * @param registration_startTime
     * The registration start time of the event.
     * @param endDate
     * The end date of the event.
     * @param registration_endTime
     * The registration end time of the event.
     * @param organizer
     * The organizer of the event.
     */

    public Event(String name, String location, String description, LocalDate startDate, LocalDateTime registration_startTime, LocalDate endDate, LocalDateTime registration_endTime, User organizer, float price) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.startDate = startDate.toString();
        this.registration_startTime = registration_startTime.toString();
        this.endDate = endDate.toString();
        this.registration_endTime = registration_endTime.toString();
        this.organizer = organizer;
        this.price = price;
    }

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        description = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        registration_endTime = in.readString();
        registration_startTime = in.readString();
        organizer = in.readParcelable(User.class.getClassLoader());
        price = in.readFloat();
        DBManager db = new DBManager(FirebaseFirestore.getInstance());
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            db.fetchUserByFirebaseId(key, new FirestoreCallbackUserReceive() {
                @Override
                public void onDataReceived(User u) {
                    applicants.put(u, value);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Event", String.format("error loading applicant: %s", e));
                }
            });
        }
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
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
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(registration_endTime);
        dest.writeString(registration_startTime);
        // store as ids when serialized
        dest.writeString(organizer.getId());
        dest.writeInt(applicants.size());
        for (Map.Entry<User, String> entry : applicants.entrySet()) {
            dest.writeString(entry.getKey().getId()); // Write custom key
            dest.writeString(entry.getValue());          // Write value
        }


    }

    /**
     * Getter for the name of the event.
     *
     * @return
     * The name of the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the event.
     *
     * @param name
     * The name of the event.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the description of the event.
     *
     * @return
     * The description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for the description of the event.
     *
     * @param description
     * The description of the event.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the location of the event.
     * @return
     * The location of the event.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for the location of the event.
     *
     * @param location
     * The location of the event.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for the start date of the event.
     *
     * @return
     * The start date of the event.
     */
    public LocalDate getStartDate() {
        return LocalDate.parse(startDate);
    }

    /**
     * Setter for the start date of the event.
     *
     * @param startDate
     * The start date of the event.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate.toString();
    }

    /**
     * Getter for the end date of the event.
     *
     * @return
     * The end date of the event.
     */
    public LocalDate getEndDate() {
        return LocalDate.parse(endDate);
    }

    /**
     * Setter for the end date of the event.
     *
     * @param endDate
     * The end date of the event.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate.toString();
    }

    /**
     * Getter for the registration end time of the event.
     *
     * @return
     * The registration end time of the event.
     */
    public LocalDateTime getRegistration_endTime() {
        return LocalDateTime.parse(registration_endTime);
    }

    /**
     * Setter for the registration end time of the event.
     * @param registration_endTime
     * The registration end time of the event.
     */
    public void setRegistration_endTime(LocalDateTime registration_endTime) {
        this.registration_endTime = registration_endTime.toString();
    }

    /**
     * Getter for the registration start time of the event.
     *
     * @return
     * The registration start time of the event.
     */
    public LocalDateTime getRegistration_startTime() {
        return LocalDateTime.parse(registration_startTime);
    }

    /**
     * Setter for the registration start time of the event.
     *
     * @param registration_startTime
     * The registration start time of the event.
     */
    public void setRegistration_startTime(LocalDateTime registration_startTime) {
        this.registration_startTime = registration_startTime.toString();
    }


    /**
     * Getter for the organizer of the event.
     *
     * @return
     * The organizer of the event.
     */
    public User getOrganizer() {
        return organizer;
    }

    /**
     * Setter for the organizer of the event.
     *
     * @param organizer
     * The organizer of the event.
     */
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }


    /**
     * adds an applicant to the waiting list of the event.
     *
     * @param applicant
     */
    //Needs to be tested
    public void addApplicant(User applicant) {
        applicants.put(applicant, "Not Drawn");
    }



    /**
     * Getter for the users in the waiting list of the event (Not Drawn).
     *
     * @return
     * The applicants of the event.
     */
    //Needs to be tested
    public List<User> getApplicants() {

        List<User> not_drawn_applicants = new ArrayList<>();


        for (Map.Entry<User, String> entry : applicants.entrySet()) {
            if (entry.getValue().equals("Not Drawn")) {
                not_drawn_applicants.add(entry.getKey());
            }
        }

        return not_drawn_applicants;

    }

    /**
     * Setter for the users in the waiting list of the event who were selected by the lottery system (Invited).
     *
     * @param winners
     * The applicants that were selected to be invited to the event.
     */
    //Needs to be tested
    public void setInvited(List<User> winners) {
        for (User user : winners) {
            applicants.replace(user, "Invited");
        }

    }

    /**
     * Getter for the users in the waiting list of the event who were invited to the event ("Invited"). The users haven't yet accepted or declined the invite.
     *
     * @return
     * The applicants that were invited to the event.
     */
    //Needs to be tested
    public List<User> getInvited() {
        List<User> invited = new ArrayList<>();

        for (Map.Entry<User, String> entry : applicants.entrySet()) {
            if (entry.getValue().equals("Invited")) {
                invited.add(entry.getKey());
            }
        }
        return invited;

    }


    /**
     * Getter for the attendees of the event ("Accepted").
     *
     * @return
     * The attendees of the event.
     */
    //Needs to be tested
    public List<User> getAttendees() {

        List<User> attendees = new ArrayList<>();

        for (Map.Entry<User, String> entry : applicants.entrySet()) {
            if (entry.getValue().equals("Accepted")) {
                attendees.add(entry.getKey());
            }
        }

        return attendees;

    }

    /**
     * Setter for an attendee of the event (Accepted).
     *
     * @param attendee
     * An attendee who accepted the invite.
     */
    //Needs to be tested
    public void setAttendee(User attendee) {
        applicants.replace(attendee, "Accepted");
    }


    /**
     * Setter for a declined attendee of the event (Declined).
     *
     * @param attendee
     * An attendee who declined the invite.
     */
    //Needs to be tested
    public void setDeclined(User attendee) {
        applicants.replace(attendee, "Declined");
    }


    /**
     * Getter for the users in the waiting list of the event who declined the invite ("Declined").
     *
     * @return
     * The applicants that declined the invite.
     */

    //Needs to be tested
    public List<User> getDeclined() {
        List<User> declined = new ArrayList<>();
        for (Map.Entry<User, String> entry : applicants.entrySet()) {
            if (entry.getValue().equals("Declined")) {
                declined.add(entry.getKey());
            }
        }
        return declined;
    }

    /**
     * If the user declined the invite, this method lets the organizer remove the user from the screen.
     *
     * @param user
     */
    public void setDeclinedRemoved(User user) {
        applicants.replace(user, "Declined-Removed");
    }

    /**
     * Getter for the ID of the event. Filled in by Firestore.
     *
     * @return
     * The ID of the event.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the ID of the event. Filled in by Firestore.
     *
     * @param id
     * The ID of the event.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Removes a user from the event.
     *
     * @param user
     * The user to remove.
     * @return
     * True if the user was removed, false otherwise.
     */
    public boolean removeUser(User user) {
        if (user == null) {
            return false;
        }
        return applicants.remove(user) != null;
    }

    public float getPrice() {
        return price;
    }

    public String getApplicantStatus(User u) {
        return applicants.get(u);
    }
}
