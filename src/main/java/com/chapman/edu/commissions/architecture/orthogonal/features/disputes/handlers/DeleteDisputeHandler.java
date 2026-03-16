package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands.DeleteDisputeCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteDisputeHandler implements CommandHandler<DeleteDisputeCommand, Void> {
    private final DisputeRepository disputeRepository;
    public DeleteDisputeHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public Void handle(DeleteDisputeCommand command) {
        if (!disputeRepository.existsById(command.id())) throw new ResourceNotFoundException("Dispute", command.id());
        disputeRepository.deleteById(command.id());
        return null;
    }
}
