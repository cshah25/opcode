package com.example.opcodeapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.Objects;

public class User implements Parcelable {

    public static final Creator<User> CREATOR = new Creator<>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @DocumentId
    private final String id;
    private final String deviceId;
    private String name;
    private String email;
    private String phoneNum;

    /**
     * Constructor for the User class.
     *
     * @param name     The name of the user.
     * @param email    The email of the user.
     * @param phoneNum The phone number of the user.
     */
    private User(String id, String deviceId, String name, String email, String phoneNum) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = deviceId;
    }

    /**
     * Constructor for the User class (for Parcelable).
     *
     * @param in The Parcel to read from.
     */
    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        phoneNum = in.readString();
        deviceId = in.readString();
    }

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
    }


    /**
     * Getter for the name of the user.
     *
     * @return The name of the user.
     */
    public String getName() {
        return name;
    }

    ;

    /**
     * Setter for the name of the user.
     *
     * @param name The name of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    ;

    /**
     * Getter for the email of the user.
     *
     * @return The email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for the email of the user.
     *
     * @param email The email of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for the phone number of the user.
     *
     * @return The phone number of the user.
     */
    public String getPhoneNum() {
        return phoneNum;
    }

    /**
     * Setter for the phone number of the user.
     *
     * @param phoneNum The phone number of the user.
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /**
     * Getter for the ID of the user. Filled in by Firestore.
     *
     * @return The ID of the user.
     */
    public String getId() {
        return id;
    }


    /**
     * Returns the device identifier associated with this user.
     * This value is used to look up the user account by device.
     *
     * @return the stored Android device identifier, or null if not set
     */
    public String getDeviceId() {
        return deviceId;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
