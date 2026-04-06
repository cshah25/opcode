package com.example.opcodeapp.model;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A user who has applied to be in a particular event
 */
public class Applicant extends AbstractModel {

    public static final Creator<Applicant> CREATOR = new Creator<>() {
        @Override
        public Applicant createFromParcel(Parcel in) {
            return new Applicant(in);
        }

        @Override
        public Applicant[] newArray(int size) {
            return new Applicant[size];
        }
    };

    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String name;
    private ApplicantStatus status;
    private LocalDateTime joinedAt;

    private Applicant(String id, String eventId, String userId, String name, ApplicantStatus status, LocalDateTime joinedAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.name = name;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    protected Applicant(Parcel in) {
        id = Objects.requireNonNull(in.readString());
        eventId = in.readString();
        userId = in.readString();
        name = in.readString();
        status = ApplicantStatus.valueOf(in.readString());
        joinedAt = DateUtil.fromParcel(in);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(eventId);
        dest.writeString(userId);
        dest.writeString(name);
        dest.writeString(status.name());
        dest.writeSerializable(joinedAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
        setDirty(true);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        setDirty(true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setDirty(true);
    }

    public ApplicantStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicantStatus status) {
        this.status = status;
        setDirty(true);
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
        setDirty(true);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        map.put("user_id", userId);
        map.put("name", name);
        map.put("status", status.name());
        map.put("joined_at", DateUtil.toSeconds(joinedAt));
        return map;
    }

    public static Applicant fromMap(String id, Map<String, Object> map) {
        String eventId = (String) map.get("event_id");
        String userId = (String) map.get("user_id");
        String name = (String) map.get("name");
        ApplicantStatus status = ApplicantStatus.valueOf((String) map.get("status"));
        LocalDateTime joinedAt = DateUtil.fromSeconds(Long.valueOf(map.get("joined_at").toString()));
        return Applicant.builder()
                .id(id)
                .eventId(eventId)
                .userId(userId)
                .name(name)
                .status(status)
                .joinedAt(joinedAt)
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
        private String eventId;
        private String userId;
        private String name;
        private ApplicantStatus status;
        private LocalDateTime joinedAt;

        public Builder id(@NonNull String id) {
            this.id = id;
            return this;
        }

        public Builder eventId(@NonNull String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder userId(@NonNull String userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(@NonNull String name) {
            this.name = name;
            return this;
        }

        public Builder status(@NonNull ApplicantStatus status) {
            this.status = status;
            return this;
        }

        public Builder joinedAt(@NonNull LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public Applicant build() {
            return new Applicant(id, eventId, userId, name, status, joinedAt);
        }
    }
}
