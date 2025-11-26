package com.chapman.edu.commissions.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a dispute in the system.
 * Disputes are created when a sales representative disagrees with a commission calculation.
 */
public class Dispute {
    private String id;
    private String calculationId;
    private String salesRepId;
    private String managerId;
    private String title;
    private String description;
    private DisputeStatus status;
    private List<DisputeComment> comments;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    private LocalDateTime resolvedDate;
    private String resolvedBy;
    private String resolution;
    private boolean isEscalated;
    
    /**
     * Default constructor
     */
    public Dispute() {
        this.comments = new ArrayList<>();
        this.status = DisputeStatus.INITIATED;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
        this.isEscalated = false;
    }
    
    /**
     * Constructor with essential fields
     */
    public Dispute(String calculationId, String salesRepId, String title, String description) {
        this();
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCalculationId() {
        return calculationId;
    }
    
    public void setCalculationId(String calculationId) {
        this.calculationId = calculationId;
    }
    
    public String getSalesRepId() {
        return salesRepId;
    }
    
    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }
    
    public String getManagerId() {
        return managerId;
    }
    
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public DisputeStatus getStatus() {
        return status;
    }
    
    public void setStatus(DisputeStatus status) {
        this.status = status;
        this.lastUpdatedDate = LocalDateTime.now();
        
        if (status == DisputeStatus.RESOLVED || status == DisputeStatus.APPROVED || status == DisputeStatus.REJECTED) {
            this.resolvedDate = LocalDateTime.now();
        }
    }
    
    public List<DisputeComment> getComments() {
        return comments;
    }
    
    public void setComments(List<DisputeComment> comments) {
        this.comments = comments;
    }
    
    public void addComment(DisputeComment comment) {
        this.comments.add(comment);
        this.lastUpdatedDate = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
    
    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }
    
    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }
    
    public String getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    public boolean isEscalated() {
        return isEscalated;
    }
    
    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
        
        if (escalated) {
            this.status = DisputeStatus.ESCALATED;
            this.lastUpdatedDate = LocalDateTime.now();
        }
    }
    
    /**
     * Add a system comment to the dispute
     * @param text the comment text
     */
    public void addSystemComment(String text) {
        DisputeComment comment = new DisputeComment(this.id, text, true);
        addComment(comment);
    }
    
    /**
     * Add a user comment to the dispute
     * @param userId the user ID
     * @param userName the user name
     * @param text the comment text
     */
    public void addUserComment(String userId, String userName, String text) {
        DisputeComment comment = new DisputeComment(this.id, userId, userName, text);
        addComment(comment);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dispute dispute = (Dispute) o;
        return Objects.equals(id, dispute.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Dispute{" +
                "id='" + id + '\'' +
                ", calculationId='" + calculationId + '\'' +
                ", salesRepId='" + salesRepId + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createdDate=" + createdDate +
                ", isEscalated=" + isEscalated +
                '}';
    }
}