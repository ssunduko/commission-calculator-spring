package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record CreateDisputeCommand(String calculationId, String salesRepId, String title, String description) implements Command<DisputeResponse> {
    public void validate() {
        if (calculationId == null || calculationId.isBlank()) throw new IllegalArgumentException("Calculation ID is required");
        if (salesRepId == null || salesRepId.isBlank()) throw new IllegalArgumentException("Sales Rep ID is required");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required");
    }
}
