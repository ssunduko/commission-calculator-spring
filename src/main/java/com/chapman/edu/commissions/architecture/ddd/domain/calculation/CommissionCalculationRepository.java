package com.chapman.edu.commissions.architecture.ddd.domain.calculation;

import java.util.List;
import java.util.Optional;

public interface CommissionCalculationRepository {
    CommissionCalculation save(CommissionCalculation calculation);
    Optional<CommissionCalculation> findById(String id);
    List<CommissionCalculation> findAll();
    List<CommissionCalculation> findByDealId(String dealId);
    List<CommissionCalculation> findBySalesRepId(String salesRepId);
}
