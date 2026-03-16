package com.chapman.edu.commissions.architecture.microservice.disputeservice;

import com.chapman.edu.commissions.architecture.microservice.disputeservice.domain.Dispute;
import com.chapman.edu.commissions.architecture.microservice.disputeservice.domain.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {
    List<Dispute> findBySalesRepId(String salesRepId);
    List<Dispute> findByStatus(DisputeStatus status);
}
