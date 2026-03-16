package com.chapman.edu.commissions.architecture.microservice.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Service Registry (Simplified)
 *
 * In production, you'd use a service discovery tool (Eureka, Consul,
 * Kubernetes DNS). This simplified registry uses configuration properties.
 */
@Component
public class ServiceRegistry {

    private final String dealServiceUrl;
    private final String planServiceUrl;
    private final String calculationServiceUrl;
    private final String disputeServiceUrl;

    public ServiceRegistry(
            @Value("${services.deal.url:http://localhost:8091}") String dealServiceUrl,
            @Value("${services.plan.url:http://localhost:8092}") String planServiceUrl,
            @Value("${services.calculation.url:http://localhost:8093}") String calculationServiceUrl,
            @Value("${services.dispute.url:http://localhost:8094}") String disputeServiceUrl) {
        this.dealServiceUrl = dealServiceUrl;
        this.planServiceUrl = planServiceUrl;
        this.calculationServiceUrl = calculationServiceUrl;
        this.disputeServiceUrl = disputeServiceUrl;
    }

    public String getDealServiceUrl() { return dealServiceUrl; }
    public String getPlanServiceUrl() { return planServiceUrl; }
    public String getCalculationServiceUrl() { return calculationServiceUrl; }
    public String getDisputeServiceUrl() { return disputeServiceUrl; }
}
