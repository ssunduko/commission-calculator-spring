package com.chapman.edu.commissions.architecture.microservice.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDealRequest(String title, BigDecimal value, String status, LocalDate closeDate) {}
