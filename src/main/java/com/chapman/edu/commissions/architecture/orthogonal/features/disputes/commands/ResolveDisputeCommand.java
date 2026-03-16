package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record ResolveDisputeCommand(String disputeId, String resolution, String resolvedBy, boolean approved) implements Command<DisputeResponse> {
    public void validate() {
        if (resolution == null || resolution.isBlank()) throw new IllegalArgumentException("Resolution is required");
        if (resolvedBy == null || resolvedBy.isBlank()) throw new IllegalArgumentException("Resolved by is required");
    }
}
