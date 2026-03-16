package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Dispute;
import com.chapman.edu.commissions.architecture.orthogonal.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands.ResolveDisputeCommand;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ValidationException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ResolveDisputeHandler implements CommandHandler<ResolveDisputeCommand, DisputeResponse> {
    private final DisputeRepository disputeRepository;
    public ResolveDisputeHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public DisputeResponse handle(ResolveDisputeCommand command) {
        Dispute dispute = disputeRepository.findById(command.disputeId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", command.disputeId()));
        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.APPROVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new ValidationException("Dispute is already resolved");
        }
        dispute.setResolution(command.resolution());
        dispute.setResolvedBy(command.resolvedBy());
        dispute.setStatus(command.approved() ? DisputeStatus.APPROVED : DisputeStatus.REJECTED);
        Dispute updated = disputeRepository.save(dispute);
        return DisputeResponse.from(updated);
    }
}
