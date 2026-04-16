package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeDocument;

import java.time.LocalDateTime;

public record DisputeDocumentResponse(
    String id,
    String name,
    String contentType,
    long sizeBytes,
    String uploadedBy,
    LocalDateTime uploadedAt
) {
    public static DisputeDocumentResponse from(DisputeDocument doc) {
        return new DisputeDocumentResponse(
            doc.getId(),
            doc.getName(),
            doc.getContentType(),
            doc.getSizeBytes(),
            doc.getUploadedBy(),
            doc.getUploadedAt()
        );
    }
}
