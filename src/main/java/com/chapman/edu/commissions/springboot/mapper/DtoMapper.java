package com.chapman.edu.commissions.springboot.mapper;

import com.chapman.edu.commissions.model.*;
import com.chapman.edu.commissions.springboot.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * DTO MAPPER — CONVERTING BETWEEN DOMAIN MODELS AND DTOs
 * ============================================================================
 *
 * CONCEPT: @Component
 * ---------------------
 * @Component is the most generic Spring stereotype annotation. It marks a class
 * as a Spring-managed bean that will be discovered during component scanning.
 *
 * Spring's component scanning hierarchy:
 *   @Component       — Generic bean (this class)
 *   ├── @Service     — Business logic layer
 *   ├── @Repository  — Data access layer (adds exception translation)
 *   ├── @Controller  — Web MVC controller (returns views)
 *   └── @RestController — REST API controller (returns JSON)
 *
 * All of these are specializations of @Component. Using the appropriate
 * stereotype makes your code more readable and enables layer-specific features.
 *
 * This mapper is marked as @Component because it doesn't fit neatly into
 * Service, Repository, or Controller categories — it's a utility bean.
 *
 * CONCEPT: Mapper Pattern
 * -------------------------
 * Mappers convert between different object representations:
 *   Domain Model → Response DTO (for API output)
 *   Request DTO → Domain Model (for API input)
 *
 * This keeps conversion logic in one place rather than scattered across
 * controllers and services. Libraries like MapStruct can automate this.
 */
@Component
public class DtoMapper {

    /**
     * Convert a Deal domain model to a DealResponse DTO.
     */
    public DealResponse toDealResponse(Deal deal) {
        DealResponse response = new DealResponse();
        response.setId(deal.getId());
        response.setTitle(deal.getTitle());
        response.setValue(deal.getValue());
        response.setStatus(deal.getStatus() != null ? deal.getStatus().name() : null);
        response.setSalesRepId(deal.getSalesRepId());
        response.setCloseDate(deal.getCloseDate());
        response.setCreatedDate(deal.getCreatedDate());

        if (deal.getProducts() != null) {
            response.setProducts(deal.getProducts().stream()
                .map(this::toProductInfo)
                .collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * Convert a DealProduct to a nested ProductInfo DTO.
     */
    private DealResponse.ProductInfo toProductInfo(DealProduct product) {
        DealResponse.ProductInfo info = new DealResponse.ProductInfo();
        info.setProductName(product.getProductName());
        info.setQuantity(product.getQuantity());
        info.setPrice(product.getPrice());
        info.setTotalPrice(product.calculateTotalPrice());
        return info;
    }

    /**
     * Convert a CommissionPlan domain model to a CommissionPlanResponse DTO.
     */
    public CommissionPlanResponse toCommissionPlanResponse(CommissionPlan plan) {
        CommissionPlanResponse response = new CommissionPlanResponse();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setCurrency(plan.getCurrency() != null ? plan.getCurrency().getCurrencyCode() : null);
        response.setStatus(plan.getStatus() != null ? plan.getStatus().name() : null);
        response.setEffectiveStartDate(plan.getEffectiveStartDate());
        response.setEffectiveEndDate(plan.getEffectiveEndDate());
        response.setCreatedDate(plan.getCreatedDate());
        response.setCreatedBy(plan.getCreatedBy());
        response.setRuleCount(plan.getRules() != null ? plan.getRules().size() : 0);
        response.setTierCount(plan.getTiers() != null ? plan.getTiers().size() : 0);
        response.setBonusCount(plan.getBonuses() != null ? plan.getBonuses().size() : 0);
        return response;
    }

    /**
     * Convert a CommissionCalculation domain model to a CommissionCalculationResponse DTO.
     */
    public CommissionCalculationResponse toCommissionCalculationResponse(CommissionCalculation calc) {
        CommissionCalculationResponse response = new CommissionCalculationResponse();
        response.setId(calc.getId());
        response.setDealId(calc.getDealId());
        response.setSalesRepId(calc.getSalesRepId());
        response.setBaseCommission(calc.getBaseCommission());
        response.setGrossCommission(calc.getGrossCommission());
        response.setNetCommission(calc.getNetCommission());
        response.setStatus(calc.getStatus() != null ? calc.getStatus().name() : null);
        response.setCalculationDate(calc.getCalculationDate());
        response.setPayoutDate(calc.getPayoutDate());
        response.setPlanId(calc.getPlanId());
        response.setCalculatedBy(calc.getCalculatedBy());
        response.setBonusCount(calc.getBonuses() != null ? calc.getBonuses().size() : 0);
        response.setAcceleratorCount(calc.getAccelerators() != null ? calc.getAccelerators().size() : 0);
        return response;
    }

    /**
     * Convert a User domain model to a UserResponse DTO.
     * NOTE: Password hash is intentionally excluded for security.
     */
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setActive(user.isActive());
        response.setDepartment(user.getDepartment());
        response.setTerritory(user.getTerritory());
        response.setCreatedDate(user.getCreatedDate());

        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet()));
        }

        return response;
    }

    /**
     * Convert a Dispute domain model to a DisputeResponse DTO.
     */
    public DisputeResponse toDisputeResponse(Dispute dispute) {
        DisputeResponse response = new DisputeResponse();
        response.setId(dispute.getId());
        response.setCalculationId(dispute.getCalculationId());
        response.setSalesRepId(dispute.getSalesRepId());
        response.setManagerId(dispute.getManagerId());
        response.setTitle(dispute.getTitle());
        response.setDescription(dispute.getDescription());
        response.setStatus(dispute.getStatus() != null ? dispute.getStatus().name() : null);
        response.setCommentCount(dispute.getComments() != null ? dispute.getComments().size() : 0);
        response.setCreatedDate(dispute.getCreatedDate());
        response.setLastUpdatedDate(dispute.getLastUpdatedDate());
        response.setResolvedDate(dispute.getResolvedDate());
        response.setResolution(dispute.getResolution());
        response.setEscalated(dispute.isEscalated());
        return response;
    }
}
