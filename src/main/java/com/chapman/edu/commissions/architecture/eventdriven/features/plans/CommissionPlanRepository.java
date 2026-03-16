package com.chapman.edu.commissions.architecture.eventdriven.features.plans;

import com.chapman.edu.commissions.architecture.eventdriven.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.eventdriven.domain.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing CommissionPlan entities.
 * Uses Spring Data JPA for database persistence.
 */
@Repository
public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, String> {

    List<CommissionPlan> findByStatus(PlanStatus status);
}
