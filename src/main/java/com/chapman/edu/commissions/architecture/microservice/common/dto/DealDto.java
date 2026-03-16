package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CONCEPT: Shared DTO (Microservice Architecture)
 *
 * In microservice architecture, services communicate via DTOs, not domain
 * entities. This DTO is shared between services so they agree on the
 * data format. It has NO dependency on any service's domain model.
 */
public record DealDto(
    String id, String title, BigDecimal value, String salesRepId,
    String status, LocalDate closeDate, LocalDate createdDate
) {}
