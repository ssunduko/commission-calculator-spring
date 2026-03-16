package com.chapman.edu.commissions.architecture.orthogonal.features.deals.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries.GetAllDealsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAllDealsHandler implements QueryHandler<GetAllDealsQuery, List<DealResponse>> {

    private final DealRepository dealRepository;

    public GetAllDealsHandler(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public List<DealResponse> handle(GetAllDealsQuery query) {
        if (query.salesRepId() != null) {
            return dealRepository.findBySalesRepId(query.salesRepId()).stream()
                    .map(DealResponse::from).collect(Collectors.toList());
        }
        if (query.status() != null) {
            return dealRepository.findByStatus(query.status()).stream()
                    .map(DealResponse::from).collect(Collectors.toList());
        }
        return dealRepository.findAll().stream()
                .map(DealResponse::from).collect(Collectors.toList());
    }
}
