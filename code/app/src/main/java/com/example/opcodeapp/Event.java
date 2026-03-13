package com.example.opcodeapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private float price;
    private int waitlistLimit;

    private User organizer;

    /**
     * A map containing all the applicants (instances of User class) of the event as keys.
     * The corresponding value of the key is the status of the applicant (i.e. "Not Drawn", "Invited", "Accepted", "Declined", "Declined-Removed").
     *
     */
    private Map<User, ApplicantStatus> applicants = new HashMap<>();

    /**
     * Needed for toObject from Firebase
     */
    public Event() {
    }

    /**
     * Constructor for the Event class.
     *
     * @param name              The name of the event.
     * @param location          The location of the event.
     * @param description       The description of the event.
     * @param start             The start date of the event.
     * @param registrationStart The registration start time of the event.
     * @param end               The end date of the event.
     * @param registrationEnd   The registration end time of the event.
     * @param organizer         The organizer of the event.
     */

    public Event(String name, String location, String description, LocalDateTime start, LocalDateTime registrationStart, LocalDateTime end, LocalDateTime registrationEnd, User organizer, float price, int waitlistLimit) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.start = start;
        this.registrationStart = registrationStart;
        this.end = end;
        this.registrationEnd = registrationEnd;
        this.organizer = organizer;
        this.price = price;
        this.waitlistLimit = waitlistLimit;
    }


    protected Event(Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        description = in.readString();
        start = (LocalDateTime) in.readSerializable();
        end = (LocalDateTime) in.readSerializable();
        registrationEnd = (LocalDateTime) in.readSerializable();
        registrationStart = (LocalDateTime) in.readSerializable();
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
                    applicants.put(u, ApplicantStatus.valueOf(value));
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
        dest.writeSerializable(start);
        dest.writeSerializable(end);
        dest.writeSerializable(registrationEnd);
        dest.writeSerializable(registrationStart);
        // store as ids when serialized
        dest.writeString(organizer.getId());
        dest.writeInt(applicants.size());
        for (Map.Entry<User, ApplicantStatus> entry : applicants.entrySet()) {
            dest.writeString(entry.getKey().getId()); // Write custom key
            dest.writeString(entry.getValue().name());          // Write value
        }


    }

    /**
     * Getter for the name of the event.
     *
     * @return The name of the event.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the event.
     *
     * @param name The name of the event.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the description of the event.
     *
     * @return The description of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for the description of the event.
     *
     * @param description The description of the event.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the location of the event.
     *
     * @return The location of the event.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for the location of the event.
     *
     * @param location The location of the event.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for the start date of the event.
     *
     * @return The start date of the event.
     */
    public LocalDateTime getStart() {
        return start;
    }


    /**
     * Setter for the start date of the event.
     *
     * @param start The start date of the event.
     */
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    /**
     * Getter for the end date of the event.
     *
     * @return The end date of the event.
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Setter for the end date of the event.
     *
     * @param end The end date of the event.
     */
    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    /**
     * Getter for the registration end time of the event.
     *
     * @return The registration end time of the event.
     */
    public LocalDateTime getRegistrationEnd() {
        return registrationEnd;
    }

    /**
     * Setter for the registration end time of the event.
     *
     * @param registrationEnd The registration end time of the event.
     */
    public void setRegistrationEnd(LocalDateTime registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /**
     * Getter for the registration start time of the event.
     *
     * @return The registration start time of the event.
     */
    public LocalDateTime getRegistrationStart() {
        return registrationStart;
    }

    /**
     * Setter for the registration start time of the event.
     *
     * @param registrationStart The registration start time of the event.
     */
    public void setRegistrationStart(LocalDateTime registrationStart) {
        this.registrationStart = registrationStart;
    }

    /**
     * Getter for the organizer of the event.
     *
     * @return The organizer of the event.
     */
    public User getOrganizer() {
        return organizer;
    }

    /**
     * Setter for the organizer of the event.
     *
     * @param organizer The organizer of the event.
     */
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }


    /**
     * Adds an applicant to the event.
     *
     * @param applicant the user joining the event
     * @param status    the status the user has with relation to the event
     */
    public void addApplicant(User applicant, ApplicantStatus status) {
        applicants.put(applicant, status);
    }

    /**
     * Adds an applicant to the waiting list of the event.
     *
     * @param applicant
     */
    //Needs to be tested
    public void addApplicant(User applicant) {
        addApplicant(applicant, ApplicantStatus.NOT_DRAWN);
    }

    /**
     * Getter for users involved with this event
     *
     * @param filter The status of the applicant to filter by
     * @return a list of users with a matching status
     */
    public List<User> getApplicants(ApplicantStatus filter) {
        return applicants.entrySet()
                .stream()
                .filter(e -> e.getValue() == filter)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    /**
     * Getter for the users in the waiting list of the event (Not Drawn).
     *
     * @return The applicants of the event.
     */
    //Needs to be tested
    public List<User> getInitialApplicants() {
        return getApplicants(ApplicantStatus.NOT_DRAWN);
    }

    /**
     * Setter for the users in the waiting list of the event who were selected by the lottery system (Invited).
     *
     * @param winners The applicants that were selected to be invited to the event.
     */
    //Needs to be tested
    public void setInvited(List<User> winners) {
        winners.forEach(u -> applicants.replace(u, ApplicantStatus.INVITED));
    }

    /**
     * Getter for the users in the waiting list of the event who were invited to the event ("Invited"). The users haven't yet accepted or declined the invite.
     *
     * @return The applicants that were invited to the event.
     */
    //Needs to be tested
    public List<User> getInvited() {
        return getApplicants(ApplicantStatus.INVITED);
    }


    /**
     * Getter for the attendees of the event ("Accepted").
     *
     * @return The attendees of the event.
     */
    //Needs to be tested
    public List<User> getAttendees() {
        return getApplicants(ApplicantStatus.ACCEPTED);
    }

    /**
     * Setter for an attendee of the event (Accepted).
     *
     * @param attendee An attendee who accepted the invite.
     */
    //Needs to be tested
    public void setAttendee(User attendee) {
        applicants.replace(attendee, ApplicantStatus.ACCEPTED);
    }


    /**
     * Setter for a declined attendee of the event (Declined).
     *
     * @param attendee An attendee who declined the invite.
     */
    //Needs to be tested
    public void setDeclined(User attendee) {
        applicants.replace(attendee, ApplicantStatus.DECLINED);
    }


    /**
     * Getter for the users in the waiting list of the event who declined the invite ("Declined").
     *
     * @return The applicants that declined the invite.
     */

    //Needs to be tested
    public List<User> getDeclined() {
        return getApplicants(ApplicantStatus.DECLINED);
    }

    /**
     * If the user declined the invite, this method lets the organizer remove the user from the screen.
     *
     * @param user
     */
    public void setDeclinedRemoved(User user) {
        applicants.replace(user, ApplicantStatus.DECLINED_REMOVED);
    }


    /**
     * Getter for the users in the waiting list of the event who declined the invite and were removed from the screen ("Declined-Removed").
     *
     * @return The applicants that declined the invite and were removed from the screen.
     */
    public List<User> getDeclinedRemoved() {
        return getApplicants(ApplicantStatus.DECLINED_REMOVED);
    }

    /**
     * Getter for the ID of the event. Filled in by Firestore.
     *
     * @return The ID of the event.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the ID of the event. Filled in by Firestore.
     *
     * @param id The ID of the event.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Removes a user from the event.
     *
     * @param user The user to remove.
     * @return True if the user was removed, false otherwise.
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

    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public ApplicantStatus getApplicantStatus(User u) {
        return applicants.get(u);
    }

    /**
     *
     * @return a mapping of the fields in this object. Used for Firestore saving
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("location", location);
        map.put("description", description);
        map.put("start", DateUtil.toLong(start));
        map.put("end", DateUtil.toLong(end));
        map.put("registrationStart", DateUtil.toLong(registrationStart));
        map.put("registrationEnd", DateUtil.toLong(registrationEnd));
        map.put("price", price);
        map.put("waitlistLimit", waitlistLimit);
        map.put("organizer_id", organizer.getId());

        Map<String, String> applicantsMap = new HashMap<>();
        applicants.forEach((user, s) -> {
            if (user != null) {
                applicantsMap.put(user.getDeviceId(), s.name());
            }
        }); //was map.put(user.getId(), s.name()); before
        map.put("applicants", applicantsMap);
        return map;
    }

    /**
     * Static function to instantiate event from a map. Used for Firebase retrieval
     *
     * @param map
     * @return
     */
    public static Event fromMap(String id, Map<String, Object> map) {
        DBManager manager = new DBManager(FirebaseFirestore.getInstance());
        String name = (String) map.get("name");
        String location = (String) map.get("location");
        String description = (String) map.get("description");
        LocalDateTime start = DateUtil.fromLong(Long.valueOf(map.get("start").toString()));
        LocalDateTime end = DateUtil.fromLong(Long.valueOf(map.get("end").toString()));
        LocalDateTime registrationStart = DateUtil.fromLong(Long.valueOf(map.get("registrationStart").toString()));
        LocalDateTime registrationEnd = DateUtil.fromLong(Long.valueOf(map.get("registrationEnd").toString()));
        float price = Float.valueOf(map.get("price").toString());
        int waitlistLimit = Integer.valueOf(map.get("waitlistLimit").toString());
        String organizer_id = (String) map.get("organizer_id");

        Event event = new Event(name, location, description, start, registrationStart, end, registrationEnd, null, price, waitlistLimit);
        event.setId(id);
        manager.fetchUserByFirebaseId(organizer_id, new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User u) {
                event.setOrganizer(u);
            }

            @Override
            public void onError(Exception e) {
                Log.e("FirestoreLoadEvent", "Error when loading organizer of event");
            }
        });

        Map<String, String> applicantsMap = (Map<String, String>) map.get("applicants");
        applicantsMap.forEach((userId, statusName) -> {
            ApplicantStatus status = ApplicantStatus.valueOf(statusName);
            manager.fetchUserByFirebaseId(userId, new FirestoreCallbackUserReceive() {
                @Override
                public void onDataReceived(User u) {
                    event.addApplicant(u, status);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("FirestoreLoadEvent", "Error when loading user with id: " + userId);
                }
            });
        });

        return event;
    }
}
