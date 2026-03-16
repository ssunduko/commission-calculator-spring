package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries.GetAllCalculationsQuery;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAllCalculationsHandler implements QueryHandler<GetAllCalculationsQuery, List<CommissionCalculationResponse>> {
    private final CommissionCalculationRepository calculationRepository;
    public GetAllCalculationsHandler(CommissionCalculationRepository calculationRepository) { this.calculationRepository = calculationRepository; }

    @Override
    public List<CommissionCalculationResponse> handle(GetAllCalculationsQuery query) {
        if (query.dealId() != null) return calculationRepository.findByDealId(query.dealId()).stream().map(CommissionCalculationResponse::from).collect(Collectors.toList());
        if (query.salesRepId() != null) return calculationRepository.findBySalesRepId(query.salesRepId()).stream().map(CommissionCalculationResponse::from).collect(Collectors.toList());
        return calculationRepository.findAll().stream().map(CommissionCalculationResponse::from).collect(Collectors.toList());
    }
}
