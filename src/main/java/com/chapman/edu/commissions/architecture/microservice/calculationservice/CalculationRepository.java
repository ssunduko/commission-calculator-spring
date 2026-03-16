package com.chapman.edu.commissions.architecture.microservice.calculationservice;

import com.chapman.edu.commissions.architecture.microservice.calculationservice.domain.CommissionCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<CommissionCalculation, String> {
    List<CommissionCalculation> findByDealId(String dealId);
    List<CommissionCalculation> findBySalesRepId(String salesRepId);
}
