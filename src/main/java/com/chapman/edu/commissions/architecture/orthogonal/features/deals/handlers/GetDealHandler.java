package com.chapman.edu.commissions.architecture.orthogonal.features.deals.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.domain.Deal;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries.GetDealQuery;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetDealHandler implements QueryHandler<GetDealQuery, DealResponse> {

    private final DealRepository dealRepository;

    public GetDealHandler(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public DealResponse handle(GetDealQuery query) {
        Deal deal = dealRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Deal", query.id()));
        return DealResponse.from(deal);
    }
}
