package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================
 * JPA ENTITY: DisputeComment
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne to Dispute: Each comment belongs to one dispute.
 *
 * This entity is a child of the Dispute aggregate.
 * Comments are created through the Dispute.addComment() helper method
 * to keep the bidirectional relationship synchronized.
 */
@Entity
@Table(name = "dispute_comments", indexes = {
        @Index(name = "idx_dc_dispute_id", columnList = "dispute_id"),
        @Index(name = "idx_dc_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
public class DisputeComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", nullable = false)
    @JsonIgnore
    private Dispute dispute;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_system_comment", nullable = false)
    private boolean isSystemComment = false;

    public DisputeComment(Dispute dispute, String userId, String userName, String text) {
        this.dispute = dispute;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.isSystemComment = false;
    }

    public DisputeComment(Dispute dispute, String text, boolean isSystemComment) {
        this.dispute = dispute;
        this.text = text;
        this.isSystemComment = isSystemComment;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisputeComment that = (DisputeComment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DisputeComment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                ", isSystemComment=" + isSystemComment +
                '}';
    }
}
