package com.chapman.edu.commissions.architecture.orthogonal.features.plans.commands;

import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record DeletePlanCommand(String id) implements Command<Void> {}
