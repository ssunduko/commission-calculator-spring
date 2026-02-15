package com.chapman.edu.commissions.springboot.repository;

import com.chapman.edu.commissions.model.Dispute;
import com.chapman.edu.commissions.model.DisputeStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HashMap-based repository for Dispute entities.
 */
@Repository
public class DisputeRepository {

    private final Map<String, Dispute> disputes = new ConcurrentHashMap<>();

    public Dispute save(Dispute dispute) {
        if (dispute.getId() == null || dispute.getId().isEmpty()) {
            dispute.setId(UUID.randomUUID().toString());
        }
        disputes.put(dispute.getId(), dispute);
        return dispute;
    }

    public Optional<Dispute> findById(String id) {
        return Optional.ofNullable(disputes.get(id));
    }

    public List<Dispute> findAll() {
        return new ArrayList<>(disputes.values());
    }

    public List<Dispute> findBySalesRepId(String salesRepId) {
        return disputes.values().stream()
                .filter(d -> salesRepId.equals(d.getSalesRepId()))
                .collect(Collectors.toList());
    }

    public List<Dispute> findByCalculationId(String calculationId) {
        return disputes.values().stream()
                .filter(d -> calculationId.equals(d.getCalculationId()))
                .collect(Collectors.toList());
    }

    public List<Dispute> findByStatus(DisputeStatus status) {
        return disputes.values().stream()
                .filter(d -> status.equals(d.getStatus()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        disputes.remove(id);
    }

    public boolean existsById(String id) {
        return disputes.containsKey(id);
    }

    public long count() {
        return disputes.size();
    }
}
