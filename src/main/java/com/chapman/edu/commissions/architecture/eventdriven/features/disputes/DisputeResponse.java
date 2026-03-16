package com.chapman.edu.commissions.architecture.eventdriven.features.disputes;

import com.chapman.edu.commissions.architecture.eventdriven.domain.Dispute;
import com.chapman.edu.commissions.architecture.eventdriven.domain.DisputeStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for dispute information.
 */
public record DisputeResponse(
    String id,
    String calculationId,
    String salesRepId,
    String title,
    String description,
    DisputeStatus status,
    boolean isEscalated,
    LocalDateTime createdDate,
    LocalDateTime resolvedDate,
    String resolution,
    int commentsCount
) {
    public static DisputeResponse from(Dispute dispute) {
        return new DisputeResponse(
            dispute.getId(),
            dispute.getCalculationId(),
            dispute.getSalesRepId(),
            dispute.getTitle(),
            dispute.getDescription(),
            dispute.getStatus(),
            dispute.isEscalated(),
            dispute.getCreatedDate(),
            dispute.getResolvedDate(),
            dispute.getResolution(),
            dispute.getComments() != null ? dispute.getComments().size() : 0
        );
    }
}
