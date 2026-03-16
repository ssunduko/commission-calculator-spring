package com.chapman.edu.commissions.architecture.orthogonal.features.calculations.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.calculations.CommissionCalculationResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;
import java.util.List;

public record GetAllCalculationsQuery(String dealId, String salesRepId) implements Query<List<CommissionCalculationResponse>> {
    public static GetAllCalculationsQuery all() { return new GetAllCalculationsQuery(null, null); }
    public static GetAllCalculationsQuery byDeal(String dealId) { return new GetAllCalculationsQuery(dealId, null); }
    public static GetAllCalculationsQuery bySalesRep(String salesRepId) { return new GetAllCalculationsQuery(null, salesRepId); }
}
