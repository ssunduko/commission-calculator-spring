package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.features.calculations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tools for Commission Calculation.
 * Exposes commission calculation operations through the Model Context Protocol.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionCalculationMcpTools {

    private final CommissionCalculationService calculationService;

    public CommissionCalculationResponse calculateCommission(String dealId, String planId) {

        log.info("MCP Tool: Calculating commission for deal={} with plan={}", dealId, planId);

        CalculateCommissionRequest request = new CalculateCommissionRequest(dealId, planId);
        return calculationService.calculateCommission(request);
    }

    public CommissionCalculationResponse getCalculation(String calculationId) {

        log.info("MCP Tool: Getting calculation with ID={}", calculationId);
        return calculationService.getCalculation(calculationId);
    }

    public List<CommissionCalculationResponse> listCalculations(String dealId, String salesRepId) {

        log.info("MCP Tool: Listing calculations with dealId={}, salesRepId={}", dealId, salesRepId);

        if (dealId != null && !dealId.isBlank()) {
            return calculationService.getCalculationsByDeal(dealId);
        } else if (salesRepId != null && !salesRepId.isBlank()) {
            return calculationService.getCalculationsBySalesRep(salesRepId);
        } else {
            return calculationService.getAllCalculations();
        }
    }
}
