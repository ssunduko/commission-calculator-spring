package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for CommissionPlan persistence operations.
 */
public interface CommissionPlanRepositoryPort {

    CommissionPlan save(CommissionPlan plan);

    Optional<CommissionPlan> findById(String id);

    List<CommissionPlan> findAll();

    List<CommissionPlan> findByStatus(PlanStatus status);

    void deleteById(String id);
}
