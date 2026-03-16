package com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;

import com.chapman.edu.commissions.architecture.orthogonal.domain.DealStatus;

import java.util.List;

/**
 * Query to retrieve deals, optionally filtered by salesRepId or status.
 */
public record GetAllDealsQuery(
    String salesRepId,
    DealStatus status
) implements Query<List<DealResponse>> {

    public static GetAllDealsQuery all() { return new GetAllDealsQuery(null, null); }
    public static GetAllDealsQuery bySalesRep(String salesRepId) { return new GetAllDealsQuery(salesRepId, null); }
    public static GetAllDealsQuery byStatus(DealStatus status) { return new GetAllDealsQuery(null, status); }
}
