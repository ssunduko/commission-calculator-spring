package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionPlanRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionPlan;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataCommissionPlanRepository extends JpaRepository<CommissionPlan, String>, CommissionPlanRepositoryPort {

    List<CommissionPlan> findByStatus(PlanStatus status);
}
