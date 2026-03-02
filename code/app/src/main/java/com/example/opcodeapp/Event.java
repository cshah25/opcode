package com.example.opcodeapp;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.auth.User;
import com.google.type.DateTime;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {

    @DocumentId
    private String id;
    private String name;
    private String location;
    private String description;
    private Date startDate;
    private Date endDate;
    private DateTime registration_startTime;
    private DateTime registration_endTime;
    private User organizer;
    private User[] applicants;
    private User[] attendees;

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

    public Event(String name, String location, String description, Date startDate, DateTime registration_startTime, Date endDate, DateTime registration_endTime, User organizer) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.startDate = startDate;
        this.registration_startTime = registration_startTime;
        this.endDate = endDate;
        this.registration_endTime = registration_endTime;
        this.organizer = organizer;
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
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Setter for the start date of the event.
     *
     * @param startDate
     * The start date of the event.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter for the end date of the event.
     *
     * @return
     * The end date of the event.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Setter for the end date of the event.
     *
     * @param endDate
     * The end date of the event.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter for the registration end time of the event.
     *
     * @return
     * The registration end time of the event.
     */
    public DateTime getRegistration_endTime() {
        return registration_endTime;
    }

    /**
     * Setter for the registration end time of the event.
     * @param registration_endTime
     * The registration end time of the event.
     */
    public void setRegistration_endTime(DateTime registration_endTime) {
        this.registration_endTime = registration_endTime;
    }

    /**
     * Getter for the registration start time of the event.
     *
     * @return
     * The registration start time of the event.
     */
    public DateTime getRegistration_startTime() {
        return registration_startTime;
    }

    /**
     * Setter for the registration start time of the event.
     *
     * @param registration_startTime
     * The registration start time of the event.
     */
    public void setRegistration_startTime(DateTime registration_startTime) {
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
    public User[] getApplicants() {
        return applicants;
    }

    /**
     * Setter for the users in the waiting list of the event.
     *
     * @param applicants
     * The applicants of the event.
     */
    public void setApplicants(User[] applicants) {
        this.applicants = applicants;
    }

    /**
     * Getter for the attendees of the event.
     *
     * @return
     * The attendees of the event.
     */
    public User[] getAttendees() {
        return attendees;
    }

    /**
     * Setter for the attendees of the event.
     *
     * @param attendees
     * The attendees of the event.
     */
    public void setAttendees(User[] attendees) {
        this.attendees = attendees;
    }
}
