package com.chapman.edu.commissions.architecture.microservice.calculationservice;

import com.chapman.edu.commissions.architecture.microservice.calculationservice.client.DealServiceClient;
import com.chapman.edu.commissions.architecture.microservice.calculationservice.client.PlanServiceClient;
import com.chapman.edu.commissions.architecture.microservice.calculationservice.domain.CommissionCalculation;
import com.chapman.edu.commissions.architecture.microservice.common.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationService.class);
    private static final BigDecimal DEFAULT_RATE = new BigDecimal("10.00");

    private final CalculationRepository calculationRepository;
    private final DealServiceClient dealServiceClient;
    private final PlanServiceClient planServiceClient;

    public CalculationService(CalculationRepository calculationRepository,
                               DealServiceClient dealServiceClient,
                               PlanServiceClient planServiceClient) {
        this.calculationRepository = calculationRepository;
        this.dealServiceClient = dealServiceClient;
        this.planServiceClient = planServiceClient;
    }

    public CalculationDto calculateCommission(CalculateCommissionRequest request) {
        request.validate();

        // Inter-service calls to get deal and plan data
        DealDto deal = dealServiceClient.getDeal(request.dealId());
        PlanDto plan = planServiceClient.getPlan(request.planId());

        log.info("Calculating commission: deal={} (value={}), plan={}", deal.id(), deal.value(), plan.name());

        BigDecimal baseCommission = deal.value()
                .multiply(DEFAULT_RATE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        CommissionCalculation calculation = new CommissionCalculation(
                deal.id(), deal.salesRepId(), baseCommission);
        calculation.setPlanId(plan.id());
        calculation.recalculate();

        CommissionCalculation saved = calculationRepository.save(calculation);
        return toDto(saved);
    }

    public CalculationDto getCalculation(String id) {
        CommissionCalculation calc = calculationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calculation not found: " + id));
        return toDto(calc);
    }

    public List<CalculationDto> getAllCalculations() {
        return calculationRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<CalculationDto> getCalculationsByDeal(String dealId) {
        return calculationRepository.findByDealId(dealId).stream().map(this::toDto).toList();
    }

    public List<CalculationDto> getCalculationsBySalesRep(String salesRepId) {
        return calculationRepository.findBySalesRepId(salesRepId).stream().map(this::toDto).toList();
    }

    private CalculationDto toDto(CommissionCalculation calc) {
        return new CalculationDto(calc.getId(), calc.getDealId(), calc.getSalesRepId(),
                calc.getPlanId(), calc.getBaseCommission(), calc.getGrossCommission(),
                calc.getNetCommission(), calc.getStatus().name(), calc.getCalculationDate());
    }
}
