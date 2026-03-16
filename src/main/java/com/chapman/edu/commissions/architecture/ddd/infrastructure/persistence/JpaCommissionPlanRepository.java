package com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence;

import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlan;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.CommissionPlanRepository;
import com.chapman.edu.commissions.architecture.ddd.domain.plan.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA implementation of the domain CommissionPlanRepository.
 * Spring Data JPA auto-generates the query methods at runtime.
 */
@Repository
public interface JpaCommissionPlanRepository extends JpaRepository<CommissionPlan, String>, CommissionPlanRepository {
    List<CommissionPlan> findByStatus(PlanStatus status);
}
