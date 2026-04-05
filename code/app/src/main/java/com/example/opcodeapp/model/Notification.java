package com.example.opcodeapp.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String id;
    private String user_id;
    private String body;
    private String event_id;
    private boolean read;
    private LocalDateTime created_at;

    public Notification(String user_id, String body, String event_id) {
        this.user_id = user_id;
        this.body = body;
        read = false;
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
        map.put("id", id);
        map.put("body", body);
        map.put("user_id", user_id);
        map.put("event_id", event_id);
        map.put("read", read);
        return map;
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
}
