package com.example.opcodeapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;
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
    private String id;
    private String deviceId;
    private String name;
    private String email;
    private String phoneNum;
    private boolean isAdmin;

    /**
     * Constructor for the User class.
     *
     * @param name     The name of the user.
     * @param email    The email of the user.
     * @param phoneNum The phone number of the user.
     */
    private User(String id, String deviceId, String name, String email, String phoneNum, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = deviceId;
        this.isAdmin = isAdmin;
    }

    /**
     * Constructor for the User class (for Parcelable).
     *
     * @param in The Parcel to read from.
     */
    protected User(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.phoneNum = in.readString();
        this.deviceId = in.readString();
        this.isAdmin = in.readBoolean();
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
        dest.writeBoolean(isAdmin);
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
     * Setter for the id of the user.
     *
     * @param id The id of the user.
     */
    public void setId(String id) {
        this.id = id;
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

    /**
     * Setter for the device id of the user.
     *
     * @param deviceId The device id of the user.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Getter for the name of the user.
     *
     * @return The name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the user.
     *
     * @param name The name of the user.
     */
    public void setName(String name) {
        this.name = name;
    }

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
     * Getter for the phone number of the user.
     *
     * @return The phone number of the user.
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Setter for the admin status of the user.
     *
     * @param admin The new admin status of the user.
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
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
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("device_id", deviceId);
        map.put("name", name);
        map.put("email", email);
        map.put("phoneNum", phoneNum);
        map.put("isAdmin", isAdmin);
        return map;
    }

    public static User fromMap(String id, Map<String, Object> map) {
        String deviceId = (String) map.get("deviceId");
        String name = (String) map.get("name");
        String email = (String) map.get("email");
        String phoneNum = (String) map.get("phoneNum");
        boolean isAdmin = Boolean.valueOf(map.get("isAdmin").toString());
        return User.builder(id)
                .name(name)
                .deviceId(deviceId)
                .email(email)
                .phoneNum(phoneNum)
                .isAdmin(isAdmin)
                .build();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /**
     * Builder class for user creation
     */
    public static class Builder {
        private final String id;
        private String deviceId;
        private String name;
        private String email;
        private String phoneNum;
        private boolean isAdmin;

        public Builder(String id) {
            this.id = id;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
            return this;
        }

        public Builder isAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }
        public User build() {
            // TODO: Validation
            return new User(id, deviceId, name, email, phoneNum, isAdmin);
        }
    }
}
