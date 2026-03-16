package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Dispute;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.commands.CreateDisputeCommand;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateDisputeHandler implements CommandHandler<CreateDisputeCommand, DisputeResponse> {
    private final DisputeRepository disputeRepository;
    public CreateDisputeHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public DisputeResponse handle(CreateDisputeCommand command) {
        Dispute dispute = new Dispute(command.calculationId(), command.salesRepId(), command.title(), command.description());
        Dispute saved = disputeRepository.save(dispute);
        return DisputeResponse.from(saved);
    }
}
