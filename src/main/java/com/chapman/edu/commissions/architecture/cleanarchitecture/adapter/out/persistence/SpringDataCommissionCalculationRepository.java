package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.CommissionCalculationRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.CommissionCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataCommissionCalculationRepository extends JpaRepository<CommissionCalculation, String>, CommissionCalculationRepositoryPort {

    List<CommissionCalculation> findByDealId(String dealId);

    List<CommissionCalculation> findBySalesRepId(String salesRepId);
}
