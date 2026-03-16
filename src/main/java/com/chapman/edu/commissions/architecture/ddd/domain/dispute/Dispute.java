package com.chapman.edu.commissions.architecture.ddd.domain.dispute;

import com.chapman.edu.commissions.architecture.ddd.domain.shared.AggregateRoot;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dispute in the system.
 * Disputes are created when a sales representative disagrees with a commission calculation.
 */
@Entity
@Table(name = "disputes")
@Data
@NoArgsConstructor
public class Dispute implements AggregateRoot {

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
    private DisputeStatus status = DisputeStatus.INITIATED;

    @Transient
    private List<DisputeComment> comments = new ArrayList<>();

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate = LocalDateTime.now();

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(length = 2000)
    private String resolution;

    @Column(name = "is_escalated", nullable = false)
    private boolean isEscalated = false;

    /**
     * Constructor with essential fields
     */
    public Dispute(String calculationId, String salesRepId, String title, String description) {
        this.calculationId = calculationId;
        this.salesRepId = salesRepId;
        this.title = title;
        this.description = description;
        this.comments = new ArrayList<>();
        this.status = DisputeStatus.INITIATED;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
        this.isEscalated = false;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
        this.lastUpdatedDate = LocalDateTime.now();

        if (status == DisputeStatus.RESOLVED || status == DisputeStatus.APPROVED || status == DisputeStatus.REJECTED) {
            this.resolvedDate = LocalDateTime.now();
        }
    }

    public void addComment(DisputeComment comment) {
        this.comments.add(comment);
        this.lastUpdatedDate = LocalDateTime.now();
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
