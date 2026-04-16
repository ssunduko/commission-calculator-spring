package com.chapman.edu.commissions.architecture.verticalslice.features.disputes;

import com.chapman.edu.commissions.architecture.verticalslice.domain.DisputeComment;

import java.time.LocalDateTime;

public record DisputeCommentResponse(
    String id,
    String userId,
    String userName,
    String text,
    LocalDateTime timestamp,
    boolean isSystemComment
) {
    public static DisputeCommentResponse from(DisputeComment comment) {
        return new DisputeCommentResponse(
            comment.getId(),
            comment.getUserId(),
            comment.getUserName(),
            comment.getText(),
            comment.getTimestamp(),
            comment.isSystemComment()
        );
    }
}
