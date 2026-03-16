package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands;

import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record DeleteDisputeCommand(String id) implements Command<Void> {}
