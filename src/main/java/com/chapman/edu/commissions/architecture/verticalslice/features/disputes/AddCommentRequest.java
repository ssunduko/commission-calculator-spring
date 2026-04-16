package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.exceptions.ValidationException;

public record AddCommentRequest(
    String userId,
    String userName,
    String text
) {
    public void validate() {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Comment text is required");
        }
    }
}
