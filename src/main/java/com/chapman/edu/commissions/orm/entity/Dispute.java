package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * JPA ENTITY: Dispute
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to CommissionCalculation: Each dispute is about one calculation.
 * - @ManyToOne to User (salesRep): The user who filed the dispute.
 * - @ManyToOne to User (manager): The manager assigned to review.
 * - @OneToMany to DisputeComment: A dispute can have many comments.
 *
 * MULTIPLE RELATIONSHIPS TO THE SAME ENTITY:
 * This entity has TWO @ManyToOne relationships to User (salesRep and manager).
 * Each needs its own @JoinColumn with a distinct column name to avoid ambiguity.
 *
 * AUDIT FIELDS:
 * createdDate, lastUpdatedDate, resolvedDate demonstrate temporal auditing.
 * In production, consider using @CreatedDate, @LastModifiedDate from
 * Spring Data JPA auditing (with @EnableJpaAuditing).
 */
@Entity
@Table(name = "disputes", indexes = {
        @Index(name = "idx_dispute_calc_id", columnList = "calculation_id"),
        @Index(name = "idx_dispute_sales_rep", columnList = "sales_rep_id"),
        @Index(name = "idx_dispute_manager", columnList = "manager_id"),
        @Index(name = "idx_dispute_status", columnList = "status"),
        @Index(name = "idx_dispute_created", columnList = "created_date")
})
@Data
@NoArgsConstructor
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * @ManyToOne to CommissionCalculation:
     * Each dispute references one specific commission calculation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculation_id", nullable = false)
    @JsonIgnore
    private CommissionCalculation calculation;

    /**
     * MULTIPLE @ManyToOne TO THE SAME ENTITY TYPE:
     * Both salesRep and manager reference the User entity.
     * Each must have a unique @JoinColumn name.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_rep_id", nullable = false)
    @JsonIgnore
    private User salesRep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private User manager;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status = DisputeStatus.INITIATED;

    /**
     * @OneToMany with CascadeType.ALL:
     * Comments are fully owned by the dispute.
     */
    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @JsonIgnore
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

    public Dispute(CommissionCalculation calculation, User salesRep, String title, String description) {
        this.calculation = calculation;
        this.salesRep = salesRep;
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
        comments.add(comment);
        comment.setDispute(this);
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public void setEscalated(boolean escalated) {
        isEscalated = escalated;
        if (escalated) {
            this.status = DisputeStatus.ESCALATED;
            this.lastUpdatedDate = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dispute that = (Dispute) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Dispute{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createdDate=" + createdDate +
                ", isEscalated=" + isEscalated +
                '}';
    }
}
