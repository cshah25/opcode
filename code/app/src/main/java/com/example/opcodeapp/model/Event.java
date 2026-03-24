package com.example.opcodeapp.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.opcodeapp.callback.FirestoreCallbackUserReceive;
import com.example.opcodeapp.repository.UserRepository;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@SuppressWarnings("deprecated")

/**
 * The Event class.
 * Contains all the information about an event.
 */
public class Event implements Parcelable {

    public static final Creator<Event> CREATOR = new Creator<>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @DocumentId
    @NonNull
    private String id;
    private String name;
    private String location;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private User organizer;
    private float price;
    private int waitlistLimit;

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

    public Event(@NonNull String id, String name, String location, String description, LocalDateTime start, LocalDateTime end, LocalDateTime registrationStart, LocalDateTime registrationEnd, User organizer, float price, int waitlistLimit) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.start = start;
        this.end = end;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.organizer = organizer;
        this.price = price;
        this.waitlistLimit = waitlistLimit;
    }

    protected Event(Parcel in) {
        id = Objects.requireNonNull(in.readString());
        name = in.readString();
        location = in.readString();
        description = in.readString();
        start = DateUtil.fromParcel(in);
        end = DateUtil.fromParcel(in);
        registrationStart = DateUtil.fromParcel(in);
        registrationEnd = DateUtil.fromParcel(in);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            organizer = in.readParcelable(User.class.getClassLoader(), User.class);
        }
        price = in.readFloat();
        waitlistLimit = in.readInt();
    }

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
        dest.writeSerializable(registrationStart);
        dest.writeSerializable(registrationEnd);
        dest.writeString(organizer.getDeviceId());
        dest.writeFloat(price);
        dest.writeInt(waitlistLimit);
    }

    /**
     * Getter for the ID of the event. Filled in by Firestore.
     *
     * @return The ID of the event.
     */
    @NonNull
    public String getId() {
        return id;
    }


    /**
     * Setter for the id of the event.
     *
     * @param id The id of the event.
     */
    public void setId(@NonNull String id) {
        this.id = id;
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
     * Getter for the price to join the event
     *
     * @return The joining price of the event
     */
    public float getPrice() {
        return price;
    }

    /**
     * Setter for the price of the event. The price is clamped to be greater than or equal to 0
     *
     * @param price The new price of the event
     */
    public void setPrice(float price) {
        this.price = Math.max(price, 0);
    }

    /**
     * Getter for the waitlist limit of the event
     *
     * @return The waitlist limit of the event
     */
    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    /**
     * Setter for the waitlist limit of the event. The waitlist limit is clamped to be greater than
     * or equal to -1 (no limit)
     *
     * @param waitlistLimit The new waitlist limit of the event
     */
    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = Math.max(waitlistLimit, -1);
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
        map.put("registration_start", DateUtil.toLong(registrationStart));
        map.put("registration_end", DateUtil.toLong(registrationEnd));
        map.put("organizer_id", organizer.getId());
        map.put("price", price);
        map.put("waitlist_limit", waitlistLimit);
        return map;
    }

    /**
     * Static function to instantiate event from a map. Used for Firebase retrieval
     *
     * @param map
     * @return TODO: Improve validation of potential null fields
     */
    public static Event fromMap(String id, Map<String, Object> map) {
        String name = (String) map.get("name");
        String location = (String) map.get("location");
        String description = (String) map.get("description");
        LocalDateTime start = DateUtil.fromLong(Long.valueOf(map.get("start").toString()));
        LocalDateTime end = DateUtil.fromLong(Long.valueOf(map.get("end").toString()));
        LocalDateTime registrationStart = DateUtil.fromLong(Long.valueOf(map.get("registration_start").toString()));
        LocalDateTime registrationEnd = DateUtil.fromLong(Long.valueOf(map.get("registration_end").toString()));
        float price = Float.valueOf(map.get("price").toString());
        int waitlistLimit = Integer.valueOf(map.get("waitlist_limit").toString());
        String organizer_id = (String) map.get("organizer_id");

        Event.Builder b = Event.builder()
                .id(id)
                .name(name)
                .location(location)
                .description(description)
                .start(start)
                .end(end)
                .registrationStart(registrationStart)
                .registrationEnd(registrationEnd)
                .price(price)
                .waitlistLimit(waitlistLimit);


        UserRepository repository = new UserRepository(FirebaseFirestore.getInstance());
        repository.fetchUser(organizer_id, new FirestoreCallbackUserReceive() {
            @Override
            public void onDataReceived(User user) {
                b.organizer(user);
            }

            @Override
            public void onError(Exception e) {
                Log.e("FirestoreLoadEvent", "Error when loading organizer of event");
            }
        });

        return b.build();
    }


    public static Builder builder() {
        return builder();
    }


    /**
     * Builder class for Events
     * TODO: Add input validation
     */
    public static class Builder {
        private String id;
        private String name;
        private String location;
        private String description;
        private LocalDateTime start;
        private LocalDateTime end;
        private LocalDateTime registrationStart;
        private LocalDateTime registrationEnd;
        private User organizer;
        private Float price;
        private Integer waitlistLimit;

        public Builder id(@NonNull String id) {
            this.id = id;
            return this;
        }

        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        public Builder location(@NonNull String location) {
            this.location = location;
            return this;
        }

        public Builder description(@NonNull String description) {
            this.description = description;
            return this;
        }

        public Builder start(LocalDateTime start) {
            this.start = start;
            return this;
        }

        public Builder end(LocalDateTime end) {
            this.end = end;
            return this;
        }

        public Builder registrationStart(LocalDateTime registrationStartTime) {
            this.registrationStart = registrationStartTime;
            return this;
        }

        public Builder registrationEnd(LocalDateTime registrationEndTime) {
            this.registrationEnd = registrationEndTime;
            return this;
        }

        public Builder organizer(User organizer) {
            this.organizer = organizer;
            return this;
        }

        public Builder waitlistLimit(int waitlistLimit) {
            this.waitlistLimit = waitlistLimit;
            return this;
        }

        public Builder price(float price) {
            this.price = price;
            return this;
        }

        public Event build() {
            LocalDateTime now = LocalDateTime.now();
            try {

            // Check if registration does not start in the past
            if (registrationStart.isBefore(now))
                throw new IllegalArgumentException("Registration start time cannot be in the past");

            // Check if registration end is after the start
            if (!registrationEnd.isAfter(registrationStart))
                throw new IllegalArgumentException("Registration end date must be after registration start");

            // Check if the event start is in the future
            if (!start.isAfter(now))
                throw new IllegalArgumentException("Event start must be in the future");

            // Check if the event end is after the start
            if (!end.isAfter(start))
                throw new IllegalArgumentException("Event end must be after event start");
            } catch (Exception e) {
                return null;
            }

            return new Event(id, name, location, description, registrationStart, registrationEnd, start, end, organizer, price, waitlistLimit);
        }
    }
}
