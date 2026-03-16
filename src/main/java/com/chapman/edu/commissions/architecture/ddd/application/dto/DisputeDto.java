package com.chapman.edu.commissions.architecture.ddd.application.dto;

import com.chapman.edu.commissions.architecture.ddd.domain.dispute.Dispute;
import com.chapman.edu.commissions.architecture.ddd.domain.dispute.DisputeStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for dispute information.
 */
public record DisputeDto(
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
    public static DisputeDto fromEntity(Dispute dispute) {
        return new DisputeDto(
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
