package com.chapman.edu.commissions.architecture.ddd.infrastructure.persistence;

import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculation;
import com.chapman.edu.commissions.architecture.ddd.domain.calculation.CommissionCalculationRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA implementation of the domain CommissionCalculationRepository.
 * Spring Data JPA auto-generates the query methods at runtime.
 */
@Repository
public interface JpaCommissionCalculationRepository extends JpaRepository<CommissionCalculation, String>, CommissionCalculationRepository {
    List<CommissionCalculation> findByDealId(String dealId);
    List<CommissionCalculation> findBySalesRepId(String salesRepId);
}
