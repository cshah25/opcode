package com.example.opcodeapp.model;

import static com.example.opcodeapp.model.AbstractModel.hasRequiredFields;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    @DocumentId
    private String id;
    private String user_id;
    private String body;
    private String event_id;
    private boolean read;
    private String destination;

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    private Timestamp created_at;

    public Notification(String user_id, String body, String event_id, String destination) {
        this.user_id = user_id;
        this.body = body;
        this.event_id = event_id;
        this.destination = destination;
        read = false;
    }

    public Notification(String id, String user_id, String body, String event_id, boolean read, Timestamp created_at, String destination) {
        this.id = id;
        this.user_id = user_id;
        this.body = body;
        this.event_id = event_id;
        this.read = read;
        this.created_at = created_at;
        this.destination = destination;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("body", body);
        map.put("userId", user_id);
        map.put("eventId", event_id);
        map.put("read", read);
        map.put("createdAt", created_at);
        map.put("destination", destination);
        return map;
    }

    public static Notification fromMap(String id, Map<String, Object> map) {
        if (!hasRequiredFields(map, "userId", "eventId", "body", "read", "createdAt", "destination")) {
            Log.w("Notification", "missing field " + map);
            return null;
        }
        return new Builder()
            .id(id)
            .body((String)map.get("body"))
            .user_id((String)map.get("userId"))
            .event_id((String)map.get("eventId"))
            .read((boolean)map.get("read"))
            .created_at((Timestamp)map.get("createdAt"))
            .destination((String)map.get("destination"))
            .build();
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public static class Builder {
        private String id;
        private String user_id;
        private String body;
        private String event_id;
        private boolean read;
        private Timestamp created_at;
        private String destination;

        public Notification.Builder id(@NonNull String id) {
            this.id = id;
            return this;
        }

        public Notification.Builder user_id(@NonNull String user_id) {
            this.user_id = user_id;
            return this;
        }

        public Notification.Builder body(@NonNull String body) {
            this.body = body;
            return this;
        }

        public Notification.Builder event_id(@NonNull String event_id) {
            this.event_id = event_id;
            return this;
        }

        public Notification.Builder read(@NonNull boolean read) {
            this.read = read;
            return this;
        }

        public Notification.Builder created_at(@NonNull Timestamp created_at) {
            this.created_at = created_at;
            return this;
        }

        public Notification.Builder destination(@NonNull String destination) {
            this.destination = destination;
            return this;
        }


        public Notification build() {
            return new Notification(id, user_id, body, event_id, read, created_at, destination);
        }
    }
}
