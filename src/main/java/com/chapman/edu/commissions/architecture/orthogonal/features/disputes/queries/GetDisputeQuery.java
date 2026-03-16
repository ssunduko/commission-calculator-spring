package com.chapman.edu.commissions.architecture.orthogonal.features.disputes.queries;

import com.chapman.edu.commissions.architecture.orthogonal.features.disputes.DisputeResponse;
import com.chapman.edu.commissions.architecture.orthogonal.pipeline.Query;

public record GetDisputeQuery(String id) implements Query<DisputeResponse> {}
