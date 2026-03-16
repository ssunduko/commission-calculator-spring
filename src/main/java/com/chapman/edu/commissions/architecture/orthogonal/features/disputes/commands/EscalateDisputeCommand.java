package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record EscalateDisputeCommand(String disputeId) implements Command<DisputeResponse> {}
