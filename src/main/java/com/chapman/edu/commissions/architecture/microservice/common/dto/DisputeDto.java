package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.time.LocalDateTime;

public record DisputeDto(
    String id, String calculationId, String salesRepId,
    String title, String description, String status,
    boolean escalated, LocalDateTime createdDate,
    String resolution, int commentsCount
) {}
