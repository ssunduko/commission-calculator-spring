package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;

public record AddDocumentRequest(
    String name,
    String contentType,
    long sizeBytes,
    String uploadedBy
) {
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Document name is required");
        }
        if (sizeBytes < 0) {
            throw new ValidationException("Document size cannot be negative");
        }
    }
}
