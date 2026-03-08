package com.example.opcodeapp;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.auth.FirebaseUser;
import com.google.type.DateTime;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Event implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private String location;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime registration_startTime;
    private LocalDateTime registration_endTime;

    private User organizer;
    private List<User> applicants;

    private List<User> attendees;

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

    public Event(String name, String location, String description, LocalDate startDate, LocalDateTime registration_startTime, LocalDate endDate, LocalDateTime registration_endTime, User organizer) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.startDate = startDate;
        this.registration_startTime = registration_startTime;
        this.endDate = endDate;
        this.registration_endTime = registration_endTime;
        this.organizer = organizer;
    }

    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        description = in.readString();
        startDate = (LocalDate) in.readSerializable();
        endDate = (LocalDate) in.readSerializable();
        registration_endTime = (LocalDateTime) in.readSerializable();
        registration_startTime = (LocalDateTime) in.readSerializable();
        organizer = in.readParcelable(User.class.getClassLoader());
        applicants = in.createTypedArrayList(User.CREATOR);
        attendees = in.createTypedArrayList(User.CREATOR);


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
        dest.writeSerializable(startDate);
        dest.writeSerializable(endDate);
        dest.writeSerializable(registration_endTime);
        dest.writeSerializable(registration_startTime);
        dest.writeParcelable(organizer, flags);
        dest.writeTypedList(applicants);
        dest.writeTypedList(attendees);
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
        return startDate;
    }

    /**
     * Setter for the start date of the event.
     *
     * @param startDate
     * The start date of the event.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter for the end date of the event.
     *
     * @return
     * The end date of the event.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Setter for the end date of the event.
     *
     * @param endDate
     * The end date of the event.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter for the registration end time of the event.
     *
     * @return
     * The registration end time of the event.
     */
    public LocalDateTime getRegistration_endTime() {
        return registration_endTime;
    }

    /**
     * Setter for the registration end time of the event.
     * @param registration_endTime
     * The registration end time of the event.
     */
    public void setRegistration_endTime(LocalDateTime registration_endTime) {
        this.registration_endTime = registration_endTime;
    }

    /**
     * Getter for the registration start time of the event.
     *
     * @return
     * The registration start time of the event.
     */
    public LocalDateTime getRegistration_startTime() {
        return registration_startTime;
    }

    /**
     * Setter for the registration start time of the event.
     *
     * @param registration_startTime
     * The registration start time of the event.
     */
    public void setRegistration_startTime(LocalDateTime registration_startTime) {
        this.registration_startTime = registration_startTime;
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
     * Getter for the users in the waiting list of the event.
     *
     * @return
     * The applicants of the event.
     */
    public List<User> getApplicants() {
        return applicants;
    }

    /**
     * Setter for the users in the waiting list of the event.
     *
     * @param applicants
     * The applicants of the event.
     */
    public void setApplicants(List<User> applicants) {
        this.applicants = applicants;
    }

    /**
     * Getter for the attendees of the event.
     *
     * @return
     * The attendees of the event.
     */
    public List<User> getAttendees() {
        return attendees;
    }

    /**
     * Setter for the attendees of the event.
     *
     * @param attendees
     * The attendees of the event.
     */
    public void setAttendees(List<User> attendees) {
        this.attendees = attendees;
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
}
