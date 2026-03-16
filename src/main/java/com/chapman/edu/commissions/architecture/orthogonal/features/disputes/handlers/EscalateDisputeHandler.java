package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Dispute;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands.EscalateDisputeCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class EscalateDisputeHandler implements CommandHandler<EscalateDisputeCommand, DisputeResponse> {
    private final DisputeRepository disputeRepository;
    public EscalateDisputeHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public DisputeResponse handle(EscalateDisputeCommand command) {
        Dispute dispute = disputeRepository.findById(command.disputeId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", command.disputeId()));
        if (dispute.isEscalated()) throw new ValidationException("Dispute is already escalated");
        dispute.setEscalated(true);
        Dispute updated = disputeRepository.save(dispute);
        return DisputeResponse.from(updated);
    }
}
