package com.example.opcodeapp;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private String email;
    private String phoneNum;
    private String deviceId;
    private List<Event> joinedEvents;
    private List<Event> createdEvents;

    /**
     * Needed for toObject from Firebase
     */
    public User() {}

    /**
     * Constructor for the User class.
     * @param name
     * The name of the user.
     * @param email
     * The email of the user.
     * @param phoneNum
     * The phone number of the user.
     */
    public User(String name, String email, String phoneNum, Context ctx) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = Settings.Secure.getString(
                ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
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
        deviceId = in.readString();
        joinedEvents = new ArrayList<>();
        createdEvents = new ArrayList<>();

        // decode ids
        DBManager db = new DBManager(FirebaseFirestore.getInstance());
        int j = in.readInt();
        for (int i = 0; i < j; i++) {
            String e_id = in.readString();
            db.fetchEventByFirebaseId(e_id, new FirestoreCallbackEventReceive() {
                @Override
                public void onDataReceived(Event e) {
                    joinedEvents.add(e);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("User", String.format("error loading joined event: %s", e));
                }
            });
        }
        j = in.readInt();
        for (int i = 0; i < j; i++) {
            String e_id = in.readString();
            db.fetchEventByFirebaseId(e_id, new FirestoreCallbackEventReceive() {
                @Override
                public void onDataReceived(Event e) {
                    createdEvents.add(e);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("User", String.format("error loading created event: %s", e));
                }
            });
        }

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
        dest.writeString(deviceId);

        // turn events into ids for serialization
        dest.writeInt(joinedEvents.size());
        for (Event e: joinedEvents) {
            dest.writeString(e.getId());
        }
        dest.writeInt(createdEvents.size());
        for (Event e: createdEvents) {
            dest.writeString(e.getId());
        }
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



    @Override
    public boolean equals(Object o) {
        // 1. Reference check: Are they the exact same instance?
        if (this == o) return true;
        // 2. Null and Class check: Is the other object null or a different type?
        if (o == null || getClass() != o.getClass()) return false;
        // 3. Field comparison: Do the significant fields match?
        User user = (User) o;
        return Objects.equals(id, user.getId()) && Objects.equals(email, user.getEmail());
    }

    @Override
    public int hashCode() {
        // Generate a hash based on the same fields used in equals()
        return Objects.hash(id, email);
    }

    public String getDeviceId() {
        return deviceId;
    }
}
