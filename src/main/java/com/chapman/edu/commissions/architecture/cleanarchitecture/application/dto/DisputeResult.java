package com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Dispute;

import java.time.LocalDateTime;

/**
 * Result DTO representing a dispute.
 */
public record DisputeResult(
        String id,
        String calculationId,
        String salesRepId,
        String title,
        String description,
        String status,
        boolean isEscalated,
        LocalDateTime createdDate,
        LocalDateTime resolvedDate,
        String resolution,
        int commentsCount
) {

    public static DisputeResult from(Dispute dispute) {
        return new DisputeResult(
                dispute.getId(),
                dispute.getCalculationId(),
                dispute.getSalesRepId(),
                dispute.getTitle(),
                dispute.getDescription(),
                dispute.getStatus() != null ? dispute.getStatus().name() : null,
                dispute.isEscalated(),
                dispute.getCreatedDate(),
                dispute.getResolvedDate(),
                dispute.getResolution(),
                dispute.getComments() != null ? dispute.getComments().size() : 0
        );
    }
}
