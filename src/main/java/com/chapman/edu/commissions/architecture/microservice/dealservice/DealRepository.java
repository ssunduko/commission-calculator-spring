package com.chapman.edu.commissions.architecture.microservice.dealservice;

import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.Deal;
import com.chapman.edu.commissions.architecture.microservice.dealservice.domain.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {
    List<Deal> findBySalesRepId(String salesRepId);
    List<Deal> findByStatus(DealStatus status);
}
