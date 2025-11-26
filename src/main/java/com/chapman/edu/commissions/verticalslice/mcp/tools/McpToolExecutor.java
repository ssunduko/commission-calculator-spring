package com.chapman.edu.commissions.verticalslice.mcp.tools;

import com.chapman.edu.commissions.verticalslice.domain.DealStatus;
import com.chapman.edu.commissions.verticalslice.domain.DisputeStatus;
import com.chapman.edu.commissions.verticalslice.domain.PlanStatus;
import com.chapman.edu.commissions.verticalslice.features.calculations.CalculateCommissionRequest;
import com.chapman.edu.commissions.verticalslice.features.calculations.CommissionCalculationService;
import com.chapman.edu.commissions.verticalslice.features.deals.*;
import com.chapman.edu.commissions.verticalslice.features.disputes.*;
import com.chapman.edu.commissions.verticalslice.features.plans.*;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class McpToolExecutor {

    private final DealService dealService;
    private final CommissionPlanService planService;
    private final CommissionCalculationService calculationService;
    private final DisputeService disputeService;
    private final ObjectMapper objectMapper;

    public McpToolExecutor(
        DealService dealService,
        CommissionPlanService planService,
        CommissionCalculationService calculationService,
        DisputeService disputeService,
        ObjectMapper objectMapper
    ) {
        this.dealService = dealService;
        this.planService = planService;
        this.calculationService = calculationService;
        this.disputeService = disputeService;
        this.objectMapper = objectMapper;
    }

    public Object executeTool(String toolName, Map<String, Object> params) {
        log.info("Executing tool: {} with params: {}", toolName, params);

        try {
            return switch (toolName) {
                case "createDeal" -> createDeal(params);
                case "getDeal" -> getDeal(params);
                case "listDeals" -> listDeals(params);
                case "updateDeal" -> updateDeal(params);

                case "createCommissionPlan" -> createPlan(params);
                case "getCommissionPlan" -> getPlan(params);
                case "listCommissionPlans" -> listPlans(params);
                case "activateCommissionPlan" -> activatePlan(params);
                case "addRuleToPlan" -> addRuleToPlan(params);

                case "calculateCommission" -> calculateCommission(params);
                case "getCalculation" -> getCalculation(params);
                case "listCalculations" -> listCalculations(params);

                case "createDispute" -> createDispute(params);
                case "resolveDispute" -> resolveDispute(params);
                case "getDispute" -> getDispute(params);
                case "listDisputes" -> listDisputes(params);

                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Error executing tool: {}", toolName, e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    private Object createDeal(Map<String, Object> params) {
        CreateDealRequest request = objectMapper.convertValue(params, CreateDealRequest.class);
        return dealService.createDeal(request);
    }

    private Object getDeal(Map<String, Object> params) {
        String dealId = (String) params.get("dealId");
        return dealService.getDeal(dealId);
    }

    private Object listDeals(Map<String, Object> params) {
        String salesRepId = (String) params.get("salesRepId");
        String statusStr = (String) params.get("status");

        if (salesRepId != null) {
            return dealService.getDealsBySalesRep(salesRepId);
        } else if (statusStr != null) {
            DealStatus status = DealStatus.valueOf(statusStr);
            return dealService.getDealsByStatus(status);
        } else {
            return dealService.getAllDeals();
        }
    }

    private Object updateDeal(Map<String, Object> params) {
        String dealId = (String) params.get("dealId");
        String title = (String) params.get("title");
        BigDecimal value = params.containsKey("amount") ? new BigDecimal(params.get("amount").toString()) : null;
        DealStatus status = params.containsKey("status") ? DealStatus.valueOf((String) params.get("status")) : null;
        LocalDate closeDate = params.containsKey("closeDate") ? LocalDate.parse((String) params.get("closeDate")) : null;

        UpdateDealRequest request = new UpdateDealRequest(title, value, status, closeDate);
        return dealService.updateDeal(dealId, request);
    }

    private Object createPlan(Map<String, Object> params) {
        CreateCommissionPlanRequest request = objectMapper.convertValue(params, CreateCommissionPlanRequest.class);
        return planService.createPlan(request);
    }

    private Object getPlan(Map<String, Object> params) {
        String planId = (String) params.get("planId");
        return planService.getPlan(planId);
    }

    private Object listPlans(Map<String, Object> params) {
        String statusStr = (String) params.get("status");
        if (statusStr != null) {
            PlanStatus status = PlanStatus.valueOf(statusStr);
            return planService.getPlansByStatus(status);
        } else {
            return planService.getAllPlans();
        }
    }

    private Object activatePlan(Map<String, Object> params) {
        String planId = (String) params.get("planId");
        return planService.activatePlan(planId);
    }

    private Object addRuleToPlan(Map<String, Object> params) {
        String planId = (String) params.get("planId");
        AddRuleToPlanRequest request = objectMapper.convertValue(params, AddRuleToPlanRequest.class);
        return planService.addRuleToPlan(planId, request);
    }

    private Object calculateCommission(Map<String, Object> params) {
        CalculateCommissionRequest request = objectMapper.convertValue(params, CalculateCommissionRequest.class);
        return calculationService.calculateCommission(request);
    }

    private Object getCalculation(Map<String, Object> params) {
        String calculationId = (String) params.get("calculationId");
        return calculationService.getCalculation(calculationId);
    }

    private Object listCalculations(Map<String, Object> params) {
        String dealId = (String) params.get("dealId");
        String salesRepId = (String) params.get("salesRepId");

        if (dealId != null) {
            return calculationService.getCalculationsByDeal(dealId);
        } else if (salesRepId != null) {
            return calculationService.getCalculationsBySalesRep(salesRepId);
        } else {
            return calculationService.getAllCalculations();
        }
    }

    private Object createDispute(Map<String, Object> params) {
        CreateDisputeRequest request = objectMapper.convertValue(params, CreateDisputeRequest.class);
        return disputeService.createDispute(request);
    }

    private Object resolveDispute(Map<String, Object> params) {
        String disputeId = (String) params.get("disputeId");
        ResolveDisputeRequest request = objectMapper.convertValue(params, ResolveDisputeRequest.class);
        return disputeService.resolveDispute(disputeId, request);
    }

    private Object getDispute(Map<String, Object> params) {
        String disputeId = (String) params.get("disputeId");
        return disputeService.getDispute(disputeId);
    }

    private Object listDisputes(Map<String, Object> params) {
        String salesRepId = (String) params.get("salesRepId");
        String statusStr = (String) params.get("status");

        if (salesRepId != null) {
            return disputeService.getDisputesBySalesRep(salesRepId);
        } else if (statusStr != null) {
            DisputeStatus status = DisputeStatus.valueOf(statusStr);
            return disputeService.getDisputesByStatus(status);
        } else {
            return disputeService.getAllDisputes();
        }
    }
}
