package com.example.opcodeapp.model;

import com.example.opcodeapp.enums.ApplicantStatus;
import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Applicant {

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
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicantStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicantStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        map.put("user_id", userId);
        map.put("name", name);
        map.put("status", status.name());
        map.put("joinedAt", DateUtil.toLong(joinedAt));
        return map;
    }

    public static Applicant fromMap(String id, Map<String, Object> map) {
        String eventId = (String) map.get("eventId");
        String userId = (String) map.get("userId");
        String name = (String) map.get("name");
        ApplicantStatus status = ApplicantStatus.valueOf((String) map.get("status"));
        LocalDateTime joinedAt = DateUtil.fromLong(Long.valueOf(map.get("phoneNum").toString()));
        return Applicant.builder(id)
                .eventId(eventId)
                .userId(userId)
                .name(name)
                .status(status)
                .joinedAt(joinedAt)
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
        private String eventId;
        private String userId;
        private String name;
        private ApplicantStatus status;
        private LocalDateTime joinedAt;

        public Builder(String id) {
            this.id = id;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder status(ApplicantStatus status) {
            this.status = status;
            return this;
        }

        public Builder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public Applicant build() {
            // TODO: Validation
            return new Applicant(id, eventId, userId, name, status, joinedAt);
        }
    }


}
