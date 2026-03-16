package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a commission dispute.
 */
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "calculation_id", nullable = false)
    private String calculationId;

    @Column(name = "sales_rep_id", nullable = false)
    private String salesRepId;

    @Column(name = "manager_id")
    private String managerId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;

    @Transient
    private List<DisputeComment> comments = new ArrayList<>();

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(length = 2000)
    private String resolution;

    @Column(name = "is_escalated", nullable = false)
    private boolean isEscalated = false;

    public Dispute() {
        this.status = DisputeStatus.INITIATED;
        this.isEscalated = false;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
        this.comments = new ArrayList<>();
    }

    public Dispute(String calculationId, String salesRepId, String title, String description) {
        this();
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
        this.title = title;
        this.description = description;
    }

    public void addComment(DisputeComment comment) {
        this.comments.add(comment);
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCalculationId() { return calculationId; }
    public void setCalculationId(String calculationId) { this.calculationId = calculationId; }
    public String getSalesRepId() { return salesRepId; }
    public void setSalesRepId(String salesRepId) { this.salesRepId = salesRepId; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus status) {
        this.status = status;
        this.lastUpdatedDate = LocalDateTime.now();
        if (status == DisputeStatus.RESOLVED || status == DisputeStatus.APPROVED || status == DisputeStatus.REJECTED) {
            this.resolvedDate = LocalDateTime.now();
        }
    }
    public List<DisputeComment> getComments() { return comments; }
    public void setComments(List<DisputeComment> comments) { this.comments = comments; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public boolean isEscalated() { return isEscalated; }
    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
        if (escalated) {
            this.status = DisputeStatus.ESCALATED;
            this.lastUpdatedDate = LocalDateTime.now();
        }
    }
}
