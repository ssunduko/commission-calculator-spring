package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;

public record GetCalculationQuery(String id) implements Query<CommissionCalculationResponse> {}
