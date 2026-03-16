package com.chapman.edu.commissions.architecture.microservice.calculationservice.client;

import com.chapman.edu.commissions.architecture.microservice.common.dto.PlanDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PlanServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PlanServiceClient.class);
    private final RestClient restClient;

    public PlanServiceClient(@Value("${services.plan.url:http://localhost:8092}") String planServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(planServiceUrl).build();
    }

    public PlanDto getPlan(String planId) {
        log.info("[INTER-SERVICE] Calling Plan Service: GET /api/plans/{}", planId);
        return restClient.get()
                .uri("/api/plans/{id}", planId)
                .retrieve()
                .body(PlanDto.class);
    }
}
