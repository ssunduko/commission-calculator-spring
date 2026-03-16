package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;

import java.util.List;
import java.util.Optional;

/**
 * Output port for CommissionCalculation persistence operations.
 */
public interface CommissionCalculationRepositoryPort {

    CommissionCalculation save(CommissionCalculation calculation);

    Optional<CommissionCalculation> findById(String id);

    List<CommissionCalculation> findAll();

    List<CommissionCalculation> findByDealId(String dealId);

    List<CommissionCalculation> findBySalesRepId(String salesRepId);

    void deleteById(String id);
}
