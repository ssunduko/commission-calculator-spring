package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.handlers;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationRepository;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries.GetCalculationQuery;
import com.chapman.edu.commissions.architecture.orthogonal.infrastructure.exceptions.ResourceNotFoundException;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCalculationHandler implements QueryHandler<GetCalculationQuery, CommissionCalculationResponse> {
    private final CommissionCalculationRepository calculationRepository;
    public GetCalculationHandler(CommissionCalculationRepository calculationRepository) { this.calculationRepository = calculationRepository; }

    @Override
    public CommissionCalculationResponse handle(GetCalculationQuery query) {
        return CommissionCalculationResponse.from(calculationRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Commission Calculation", query.id())));
    }
}
