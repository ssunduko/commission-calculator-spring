package com.chapman.edu.commissions.architecture.orthogonal.features.deals.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.deals.DealResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;

public record GetDealQuery(String id) implements Query<DealResponse> {
}
