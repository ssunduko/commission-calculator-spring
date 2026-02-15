package com.chapman.edu.commissions.springboot.repository;

import com.chapman.edu.commissions.model.CommissionCalculation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HashMap-based repository for CommissionCalculation entities.
 */
@Repository
public class CommissionCalculationRepository {

    private final Map<String, CommissionCalculation> calculations = new ConcurrentHashMap<>();

    public CommissionCalculation save(CommissionCalculation calculation) {
        if (calculation.getId() == null || calculation.getId().isEmpty()) {
            calculation.setId(UUID.randomUUID().toString());
        }
        calculations.put(calculation.getId(), calculation);
        return calculation;
    }

    public Optional<CommissionCalculation> findById(String id) {
        return Optional.ofNullable(calculations.get(id));
    }

    public List<CommissionCalculation> findAll() {
        return new ArrayList<>(calculations.values());
    }

    public List<CommissionCalculation> findByDealId(String dealId) {
        return calculations.values().stream()
                .filter(calc -> dealId.equals(calc.getDealId()))
                .collect(Collectors.toList());
    }

    public List<CommissionCalculation> findBySalesRepId(String salesRepId) {
        return calculations.values().stream()
                .filter(calc -> salesRepId.equals(calc.getSalesRepId()))
                .collect(Collectors.toList());
    }

    public List<CommissionCalculation> findByStatus(CommissionCalculation.CommissionStatus status) {
        return calculations.values().stream()
                .filter(calc -> status.equals(calc.getStatus()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        calculations.remove(id);
    }

    public boolean existsById(String id) {
        return calculations.containsKey(id);
    }

    public long count() {
        return calculations.size();
    }
}
