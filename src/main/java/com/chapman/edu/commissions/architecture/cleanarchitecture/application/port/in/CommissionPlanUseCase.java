package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.AddRuleCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CreatePlanCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.PlanResult;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;

import java.util.List;

/**
 * Input port defining commission plan management use cases.
 */
public interface CommissionPlanUseCase {

    PlanResult createPlan(CreatePlanCommand command);

    PlanResult getPlan(String id);

    List<PlanResult> getAllPlans();

    List<PlanResult> getPlansByStatus(PlanStatus status);

    PlanResult activatePlan(String id);

    PlanResult addRuleToPlan(String planId, AddRuleCommand command);

    void deletePlan(String id);
}
