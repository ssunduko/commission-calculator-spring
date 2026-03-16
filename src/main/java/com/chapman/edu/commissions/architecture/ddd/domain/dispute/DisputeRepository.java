package com.chapman.edu.commissions.architecture.ddd.domain.dispute;

import java.util.List;
import java.util.Optional;

public interface DisputeRepository {
    Dispute save(Dispute dispute);
    Optional<Dispute> findById(String id);
    List<Dispute> findAll();
    List<Dispute> findBySalesRepId(String salesRepId);
    List<Dispute> findByStatus(DisputeStatus status);
    boolean existsById(String id);
    void deleteById(String id);
}
