package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.domain.Dispute;
import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    int commentsCount,
    List<DisputeDocumentResponse> documents,
    List<DisputeCommentResponse> comments
) {
    public static DisputeResponse from(Dispute dispute) {
        List<DisputeDocumentResponse> docs = dispute.getDocuments() == null
            ? Collections.emptyList()
            : dispute.getDocuments().stream()
                .map(DisputeDocumentResponse::from)
                .collect(Collectors.toList());
        List<DisputeCommentResponse> comments = dispute.getComments() == null
            ? Collections.emptyList()
            : dispute.getComments().stream()
                .map(DisputeCommentResponse::from)
                .collect(Collectors.toList());
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
            comments.size(),
            docs,
            comments
        );
    }
}
