package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries.GetAllDisputesQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAllDisputesHandler implements QueryHandler<GetAllDisputesQuery, List<DisputeResponse>> {
    private final DisputeRepository disputeRepository;
    public GetAllDisputesHandler(DisputeRepository disputeRepository) { this.disputeRepository = disputeRepository; }

    @Override
    public List<DisputeResponse> handle(GetAllDisputesQuery query) {
        if (query.salesRepId() != null) return disputeRepository.findBySalesRepId(query.salesRepId()).stream().map(DisputeResponse::from).collect(Collectors.toList());
        if (query.status() != null) return disputeRepository.findByStatus(query.status()).stream().map(DisputeResponse::from).collect(Collectors.toList());
        return disputeRepository.findAll().stream().map(DisputeResponse::from).collect(Collectors.toList());
    }
}
