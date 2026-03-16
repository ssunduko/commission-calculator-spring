package com.chapman.edu.commissions.architecture.orthogonal.features.deals.commands;

import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Command;

public record DeleteDealCommand(String id) implements Command<Void> {
}
