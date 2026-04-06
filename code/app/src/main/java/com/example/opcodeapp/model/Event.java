package com.example.opcodeapp.model;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * The Event class.
 * Contains all the information about an event.
 */
public class Event extends AbstractModel {

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
    private String organizerId;
    private float price;
    private int waitlistLimit;
    private int waitlistCount;
    private boolean isPublic;
    private String encodedImage;

    /**
     * Constructor for the Event class.
     *
     * @param id                The id of the event.
     * @param name              The name of the event.
     * @param location          The location of the event.
     * @param description       The description of the event.
     * @param start             The start date of the event.
     * @param end               The end date of the event.
     * @param registrationStart The registration start time of the event.
     * @param registrationEnd   The registration end time of the event.
     * @param organizerId       The organizer id of the event.
     * @param price             The price to join the event.
     * @param waitlistLimit     The maximum number of people that can join the waitlist.
     * @param waitlistCount     The current number of people that are in the waitlist.
     * @param isPublic          The public status of the event.
     * @param encodedImage      The encoded string of the event's poster image.
     */
    public Event(@NonNull String id, String name, String location, String description,
                 LocalDateTime start, LocalDateTime end, LocalDateTime registrationStart,
                 LocalDateTime registrationEnd, String organizerId, float price, int waitlistLimit,
                 int waitlistCount, boolean isPublic, String encodedImage) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.start = start;
        this.end = end;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.organizerId = organizerId;
        this.price = price;
        this.waitlistLimit = waitlistLimit;
        this.waitlistCount = waitlistCount;
        this.isPublic = isPublic;
        this.encodedImage = encodedImage;
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
        organizerId = in.readString();
        price = in.readFloat();
        waitlistLimit = in.readInt();
        waitlistCount = in.readInt();
        isPublic = in.readBoolean();
        encodedImage = in.readString();
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
        dest.writeString(organizerId);
        dest.writeFloat(price);
        dest.writeInt(waitlistLimit);
        dest.writeInt(waitlistCount);
        dest.writeBoolean(isPublic);
        dest.writeString(encodedImage);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
    }

    /**
     * Getter for the organizer of the event.
     *
     * @return The organizer of the event.
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Setter for the organizer of the event.
     *
     * @param organizerId The organizer id of the event.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
    }

    /**
     * Getter for the current waitlist count.
     *
     * @return The number of users currently on the waitlist.
     */
    public int getWaitlistCount() {
        return waitlistCount;
    }

    /**
     * Setter for the waitlist count.
     *
     * @param waitlistCount The new waitlist count.
     */
    public void setWaitlistCount(int waitlistCount) {
        this.waitlistCount = Math.max(waitlistCount, 0);
        setDirty(true);
    }

    /**
     * @return {@code #true} if the event is public to all applicants, {@code false} otherwise
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Setter for the public status of the event
     *
     * @param isPublic The new public status
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * @return The image of the event poster encoded in Base64
     */
    public String getEncodedImage() {
        return encodedImage;
    }

    /**
     * Setter for the image of the event poster encoded in Base64
     *
     * @param encodedImage The new encoded image string
     */
    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    /**
     * @return the date period of the events formatted iin dd/MM/yyyy
     */
    public String getFormattedDates() {
        return DateUtil.toString(start) + " - " + DateUtil.toString(end);
    }

    /**
     * @return the registration period of the events formatted iin dd/MM/yyyy
     */
    public String getFormattedRegistration() {
        return DateUtil.toString(registrationStart) + " - " + DateUtil.toString(registrationEnd);
    }

    /**
     * Increments the waitlist count
     */
    public void incrementWaitlistCount() {
        setWaitlistCount(waitlistCount + 1);
    }

    /**
     * Decrements the waitlist count
     */
    public void decrementWaitlistCount() {
        setWaitlistCount(waitlistCount - 1);
    }

    /**
     * @return a mapping of the fields in this object. Used for Firestore saving
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("location", location);
        map.put("description", description);
        map.put("start", DateUtil.toSeconds(start));
        map.put("end", DateUtil.toSeconds(end));
        map.put("registration_start", DateUtil.toSeconds(registrationStart));
        map.put("registration_end", DateUtil.toSeconds(registrationEnd));
        map.put("organizer_id", organizerId);
        map.put("price", price);
        map.put("waitlist_limit", waitlistLimit);
        map.put("waitlist_count", waitlistCount);
        map.put("is_public", isPublic);
        map.put("encoded_image", encodedImage);
        return map;
    }

    /**
     * Static function to instantiate event from a map. Used for Firebase retrieval
     *
     * @param id  The Firestore id of the Event's document
     * @param map The data map retrieved from the {@link com.google.firebase.firestore.QueryDocumentSnapshot}
     * @return TODO: Improve validation of potential null fields
     */
    public static Event fromMap(String id, Map<String, Object> map) {
        if (!hasRequiredFields(map, "name", "location", "description", "start", "end",
                "registration_start", "registration_end", "organizer_id", "price", "waitlist_limit",
                "waitlist_count", "is_public", "encoded_image"))
            return null;

        String name = (String) map.get("name");
        String location = (String) map.get("location");
        String description = (String) map.get("description");
        LocalDateTime start = DateUtil.fromSeconds(Long.valueOf(map.get("start").toString()));
        LocalDateTime end = DateUtil.fromSeconds(Long.valueOf(map.get("end").toString()));
        LocalDateTime registrationStart = DateUtil.fromSeconds(Long.valueOf(map.get("registration_start").toString()));
        LocalDateTime registrationEnd = DateUtil.fromSeconds(Long.valueOf(map.get("registration_end").toString()));
        String organizerId = (String) map.get("organizer_id");
        float price = Float.valueOf(map.get("price").toString());
        int waitlistLimit = Integer.valueOf(map.get("waitlist_limit").toString());
        int waitlistCount = Integer.valueOf(map.get("waitlist_count").toString());
        boolean isPublic = Boolean.valueOf(map.get("is_public").toString());
        String encodedImage = map.get("encoded_image").toString();

        return Event.builder()
                .id(id)
                .name(name)
                .location(location)
                .description(description)
                .start(start)
                .end(end)
                .registrationStart(registrationStart)
                .registrationEnd(registrationEnd)
                .organizerId(organizerId)
                .price(price)
                .waitlistLimit(waitlistLimit)
                .waitlistCount(waitlistCount)
                .isPublic(isPublic)
                .encodedImage(encodedImage)
                .build();
    }


    public static Builder builder() {
        return new Builder();
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
        private String organizerId;
        private float price;
        private int waitlistLimit;
        private int waitlistCount;
        private boolean isPublic;
        private String encodedImage;

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

        public Builder start(@NonNull LocalDateTime start) {
            this.start = start;
            return this;
        }

        public Builder end(@NonNull LocalDateTime end) {
            this.end = end;
            return this;
        }

        public Builder registrationStart(@NonNull LocalDateTime registrationStartTime) {
            this.registrationStart = registrationStartTime;
            return this;
        }

        public Builder registrationEnd(@NonNull LocalDateTime registrationEndTime) {
            this.registrationEnd = registrationEndTime;
            return this;
        }

        public Builder organizerId(@NonNull String organizerId) {
            this.organizerId = organizerId;
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

        public Builder waitlistCount(int waitlistCount) {
            this.waitlistCount = waitlistCount;
            return this;
        }

        public Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public Builder encodedImage(String encodedImage) {
            this.encodedImage = encodedImage;
            return this;
        }

        public Event build() {
            try {
                // Check if registration end is after the registration start
                if (!registrationEnd.isAfter(registrationStart))
                    throw new IllegalArgumentException("Registration end date must be after registration start");

                // Check if the event end is after the start
                if (!end.isAfter(start))
                    throw new IllegalArgumentException("Event end must be after event start");
            } catch (Exception e) {
                return null;
            }

            return new Event(id, name, location, description, start, end,
                    registrationStart, registrationEnd, organizerId, price, waitlistLimit, waitlistCount, isPublic, encodedImage);
        }
    }
}
