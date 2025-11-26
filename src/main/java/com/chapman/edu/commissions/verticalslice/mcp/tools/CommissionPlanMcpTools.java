package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.verticalslice.features.plans.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MCP Tools for Commission Plan Management.
 * Exposes commission plan operations through the Model Context Protocol.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionPlanMcpTools {

    private final CommissionPlanService planService;

    public CommissionPlanResponse createCommissionPlan(String name, String currencyCode, String effectiveStartDate, String effectiveEndDate) {

        log.info("MCP Tool: Creating commission plan with name={}, currency={}", name, currencyCode);

        LocalDate startDate = (effectiveStartDate != null && !effectiveStartDate.isBlank())
                ? LocalDate.parse(effectiveStartDate)
                : null;

        LocalDate endDate = (effectiveEndDate != null && !effectiveEndDate.isBlank())
                ? LocalDate.parse(effectiveEndDate)
                : null;

        CreateCommissionPlanRequest request = new CreateCommissionPlanRequest(
                name, currencyCode, startDate, endDate
        );
        return planService.createPlan(request);
    }

    public CommissionPlanResponse getCommissionPlan(String planId) {

        log.info("MCP Tool: Getting commission plan with ID={}", planId);
        return planService.getPlan(planId);
    }

    public List<CommissionPlanResponse> listCommissionPlans(String status) {

        log.info("MCP Tool: Listing commission plans with status={}", status);

        if (status != null && !status.isBlank()) {
            PlanStatus planStatus = PlanStatus.valueOf(status.toUpperCase());
            return planService.getPlansByStatus(planStatus);
        } else {
            return planService.getAllPlans();
        }
    }

    public CommissionPlanResponse activateCommissionPlan(String planId) {

        log.info("MCP Tool: Activating commission plan ID={}", planId);
        return planService.activatePlan(planId);
    }

    public CommissionPlanResponse addRuleToPlan(String planId, String name, BigDecimal rate, String description, String ruleType, Integer priority) {

        log.info("MCP Tool: Adding rule to plan ID={}, rule name={}", planId, name);

        AddRuleToPlanRequest request = new AddRuleToPlanRequest(
                name,
                description,
                rate,
                ruleType,
                priority != null ? priority : 0
        );
        return planService.addRuleToPlan(planId, request);
    }

    public String deleteCommissionPlan(String planId) {

        log.info("MCP Tool: Deleting commission plan ID={}", planId);
        planService.deletePlan(planId);
        return "Commission plan " + planId + " deleted successfully";
    }
}
