package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries;

import com.chapman.edu.commissions.architecture.orthogonal.domain.DisputeStatus;
import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;
import java.util.List;

public record GetAllDisputesQuery(String salesRepId, DisputeStatus status) implements Query<List<DisputeResponse>> {
    public static GetAllDisputesQuery all() { return new GetAllDisputesQuery(null, null); }
    public static GetAllDisputesQuery bySalesRep(String salesRepId) { return new GetAllDisputesQuery(salesRepId, null); }
    public static GetAllDisputesQuery byStatus(DisputeStatus status) { return new GetAllDisputesQuery(null, status); }
}
