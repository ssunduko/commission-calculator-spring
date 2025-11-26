package com.chapman.edu.commissions.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a comment on a dispute.
 */
public class DisputeComment {
    private String id;
    private String disputeId;
    private String userId;
    private String userName;
    private String text;
    private LocalDateTime timestamp;
    private boolean isSystemComment;
    
    /**
     * Default constructor
     */
    public DisputeComment() {
        this.timestamp = LocalDateTime.now();
        this.isSystemComment = false;
    }
    
    /**
     * Constructor with essential fields
     */
    public DisputeComment(String disputeId, String userId, String userName, String text) {
        this();
        this.disputeId = disputeId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
    }
    
    /**
     * Constructor for system comments
     */
    public DisputeComment(String disputeId, String text, boolean isSystemComment) {
        this();
        this.disputeId = disputeId;
        this.text = text;
        this.isSystemComment = isSystemComment;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDisputeId() {
        return disputeId;
    }
    
    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSystemComment() {
        return isSystemComment;
    }
    
    public void setSystemComment(boolean systemComment) {
        isSystemComment = systemComment;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisputeComment that = (DisputeComment) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DisputeComment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", timestamp=" + timestamp +
                ", isSystemComment=" + isSystemComment +
                '}';
    }
}