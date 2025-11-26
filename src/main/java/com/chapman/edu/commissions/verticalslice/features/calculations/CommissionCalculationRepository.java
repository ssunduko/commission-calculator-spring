package com.chapman.edu.commissions.verticalslice.features.calculations;

import com.chapman.edu.commissions.verticalslice.domain.CommissionCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing CommissionCalculation entities.
 * Uses Spring Data JPA for database persistence.
 */
@Repository
public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, String> {

    List<CommissionCalculation> findByDealId(String dealId);

    List<CommissionCalculation> findBySalesRepId(String salesRepId);
}
