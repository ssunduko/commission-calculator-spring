package com.chapman.edu.commissions.architecture.ddd.domain.plan;

import java.util.List;
import java.util.Optional;

public interface CommissionPlanRepository {
    CommissionPlan save(CommissionPlan plan);
    Optional<CommissionPlan> findById(String id);
    List<CommissionPlan> findAll();
    List<CommissionPlan> findByStatus(PlanStatus status);
    boolean existsById(String id);
    void deleteById(String id);
}
