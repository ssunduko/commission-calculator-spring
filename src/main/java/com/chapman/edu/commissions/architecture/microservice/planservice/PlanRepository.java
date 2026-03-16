package com.chapman.edu.commissions.architecture.microservice.planservice;

import com.chapman.edu.commissions.architecture.microservice.planservice.domain.CommissionPlan;
import com.chapman.edu.commissions.architecture.microservice.planservice.domain.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<CommissionPlan, String> {
    List<CommissionPlan> findByStatus(PlanStatus status);
}
