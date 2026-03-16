package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.DealRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.Deal;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataDealRepository extends JpaRepository<Deal, String>, DealRepositoryPort {

    List<Deal> findBySalesRepId(String salesRepId);

    List<Deal> findByStatus(DealStatus status);
}
