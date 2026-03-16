package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.in;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculateCommissionCommand;
import com.chapman.edu.commissions.architecture.cleanarchitecture.application.dto.CalculationResult;

import java.util.List;

/**
 * Input port defining commission calculation use cases.
 */
public interface CommissionCalculationUseCase {

    CalculationResult calculateCommission(CalculateCommissionCommand command);

    CalculationResult getCalculation(String id);

    List<CalculationResult> getAllCalculations();

    List<CalculationResult> getCalculationsByDeal(String dealId);

    List<CalculationResult> getCalculationsBySalesRep(String salesRepId);

    void deleteCalculation(String id);
}
