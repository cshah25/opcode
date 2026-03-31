package com.example.opcodeapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.opcodeapp.util.DateUtil;
import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Comment implements Parcelable {

    public static final Creator<Comment> CREATOR = new Creator<>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };





    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String content;

    private LocalDateTime commentTime;




    public Comment(@NonNull String id, String eventId, String userId, String content, LocalDateTime commentTime) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.content = content;
        this.commentTime = commentTime;
    }








    protected Comment(Parcel in) {
        id = Objects.requireNonNull(in.readString());
        eventId = in.readString();
        userId = in.readString();
        content = in.readString();
        commentTime = DateUtil.fromParcel(in);

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
        dest.writeString(content);
        dest.writeSerializable(commentTime);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(LocalDateTime commentTime) {
        this.commentTime = commentTime;
    }


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        map.put("user_id", userId);
        map.put("content", content);
        map.put("comment_time", DateUtil.toLong(commentTime));
        return map;
    }



    public static Comment fromMap(String id, Map<String, Object> map) {
        String eventId = (String) map.get("event_id");
        String userId = (String) map.get("user_id");
        String content = (String) map.get("content");
        LocalDateTime commentTime = DateUtil.fromLong(Long.valueOf(map.get("comment_time").toString()));
        return Comment.builder()
                .id(id)
                .eventId(eventId)
                .userId(userId)
                .content(content)
                .time(commentTime)
                .build();
    }


    public static Comment.Builder builder() {
        return new Comment.Builder();
    }

    /**
     * Builder class for user creation
     */
    public static class Builder {
        private String comment_id;
        private String eventid;
        private String userid;
        private String comment_content;
        private LocalDateTime comment_time;

        public Comment.Builder id(@NonNull String id) {
            this.comment_id = id;
            return this;
        }

        public Comment.Builder eventId(@NonNull String eventId) {
            this.eventid = eventId;
            return this;
        }

        public Comment.Builder userId(@NonNull String userId) {
            this.userid = userId;
            return this;
        }

        public Comment.Builder content(@NonNull String content) {
            this.comment_content = content;
            return this;
        }

        public Comment.Builder time(@NonNull LocalDateTime time) {
            this.comment_time = time;
            return this;
        }






        public Comment build() {
            // TODO: Validation
            return new Comment(comment_id, eventid, comment_id, comment_content, comment_time);
        }
    }





}
