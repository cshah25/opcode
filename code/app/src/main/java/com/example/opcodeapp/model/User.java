package com.example.opcodeapp.model;

import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.opcodeapp.util.ValidationUtil;
import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * User class separate from any events
 */
public class User extends AbstractModel {

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
    private String fcmToken;
    private double latitude;
    private double longitude;
    private boolean isAdmin;

    /**
     * Constructor for the User class.
     *
     * @param name     The name of the user.
     * @param email    The email of the user.
     * @param phoneNum The phone number of the user.
     */
    private User(String id, String deviceId, String name, String email, String phoneNum, String fcmToken, boolean isAdmin, double latitute, double longitude) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceId = deviceId;
        this.fcmToken = fcmToken;
        this.isAdmin = isAdmin;
        this.latitude = latitute;
        this.longitude = longitude;
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
        this.fcmToken = in.readString();
        this.isAdmin = in.readBoolean();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
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
        dest.writeString(fcmToken);
        dest.writeBoolean(isAdmin);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
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
        setDirty(true);
    }

    /**
     * Getter for the user's latitude
     *
     * @return The user's latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude associated with this user or location.
     *
     * <p>Marks the object as dirty so the updated value can be persisted.
     *
     * @param latitude the latitude coordinate in decimal degrees
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        setDirty(true);
    }

    /**
     * Returns the longitude associated with this user or location.
     *
     * @return the longitude coordinate in decimal degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude associated with this user or location.
     *
     * <p>Marks the object as dirty so the updated value can be persisted.
     *
     * @param longitude the longitude coordinate in decimal degrees
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
        setDirty(true);
    }

    /**
     * Returns the Firebase Cloud Messaging token for this user.
     *
     * @return the FCM token used for push notifications
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Sets the Firebase Cloud Messaging token for this user.
     *
     * @param fcmToken the FCM token used for push notifications
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
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
        map.put("phone_num", phoneNum);
        map.put("fcm_token", fcmToken);
        map.put("is_admin", isAdmin);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        return map;
    }

    public static User fromMap(String id, Map<String, Object> map) {
        if (!hasRequiredFields(map, "device_id", "name", "email", "phone_num", "is_admin", "latitude", "longitude", "fcm_token"))
            return null;

        String deviceId = (String) map.get("device_id");
        String name = (String) map.get("name");
        String email = (String) map.get("email");
        String phoneNum = (String) map.get("phone_num");
        String fcmToken = (String) map.get("fcm_token");
        boolean isAdmin = Boolean.parseBoolean(map.get("is_admin").toString());
        double latitude = Double.parseDouble(map.get("latitude").toString());
        double longitude = Double.parseDouble(map.get("longitude").toString());

        return User.builder()
                .id(id)
                .name(name)
                .deviceId(deviceId)
                .email(email)
                .phoneNum(phoneNum)
                .fcmToken(fcmToken)
                .isAdmin(isAdmin)
                .latitute(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for user creation
     */
    public static class Builder {
        private String id;
        private String deviceId;
        private String name;
        private String email;
        private String phoneNum;
        private String fcmToken;
        private double latitude;
        private double longitude;
        private boolean isAdmin;

        public Builder id(@NonNull String id) {
            this.id = id;
            return this;
        }

        public Builder deviceId(@NonNull String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        public Builder email(@NonNull String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNum(@NonNull String phoneNum) {
            this.phoneNum = phoneNum;
            return this;
        }

        public Builder fcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
            return this;
        }

        public Builder isAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public Builder latitute(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }


        public User build() {
            // Validate that name and email were non-empty
            if (name.trim().isEmpty() || email.trim().isEmpty()) {
                Log.e("User.Builder", "Name or Email is missing");
                return null;
            }

            // Validate email
            if (!ValidationUtil.isValidEmail(email)) {
                Log.e("User.Builder", "Email is invalid");
                return null;
            }

            // Validate phone number (if provided)
            if (phoneNum != null && !phoneNum.trim().isEmpty() && !ValidationUtil.isValidPhoneNumber(phoneNum)) {
                Log.e("User.Builder", "Phone number is invalid");
                return null;
            }

            return new User(id, deviceId, name, email, phoneNum, fcmToken, isAdmin, latitude, longitude);
        }
    }
}
