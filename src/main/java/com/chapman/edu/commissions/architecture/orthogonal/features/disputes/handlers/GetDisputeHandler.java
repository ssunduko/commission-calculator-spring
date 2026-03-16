package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries.GetDisputeQuery;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetDisputeHandler implements QueryHandler<GetDisputeQuery, DisputeResponse> {
    private final DisputeRepository disputeRepository;
    public GetDisputeHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public DisputeResponse handle(GetDisputeQuery query) {
        return DisputeResponse.from(disputeRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", query.id())));
    }
}
