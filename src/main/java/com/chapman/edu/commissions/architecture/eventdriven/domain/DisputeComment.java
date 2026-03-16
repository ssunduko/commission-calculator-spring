package com.chapman.edu.commissions.architecture.eventdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a comment on a dispute.
 */
@Entity
@Table(name = "dispute_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisputeComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "dispute_id", nullable = false)
    private String disputeId;

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

    /**
     * Constructor with essential fields
     */
    public DisputeComment(String disputeId, String userId, String userName, String text) {
        this.disputeId = disputeId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.isSystemComment = false;
    }

    /**
     * Constructor for system comments
     */
    public DisputeComment(String disputeId, String text, boolean isSystemComment) {
        this.disputeId = disputeId;
        this.text = text;
        this.isSystemComment = isSystemComment;
        this.timestamp = LocalDateTime.now();
    }
}
