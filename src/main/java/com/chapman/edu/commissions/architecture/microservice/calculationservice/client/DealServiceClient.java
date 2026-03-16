package com.chapman.edu.commissions.architecture.microservice.calculationservice.client;

import com.chapman.edu.commissions.architecture.microservice.common.dto.DealDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * CONCEPT: Service-to-Service Communication (Microservice Architecture)
 *
 * In microservices, each service owns its own data. When the Calculation
 * Service needs deal information, it calls the Deal Service via REST.
 * This is fundamentally different from monolithic architectures where
 * services share a database.
 *
 * Trade-offs:
 * - PRO: Services are truly independent and can be deployed separately
 * - CON: Network latency and potential failures in inter-service calls
 * - CON: Data consistency is eventual, not immediate
 */
@Component
public class DealServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DealServiceClient.class);
    private final RestClient restClient;

    public DealServiceClient(@Value("${services.deal.url:http://localhost:8091}") String dealServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(dealServiceUrl).build();
    }

    public DealDto getDeal(String dealId) {
        log.info("[INTER-SERVICE] Calling Deal Service: GET /api/deals/{}", dealId);
        return restClient.get()
                .uri("/api/deals/{id}", dealId)
                .retrieve()
                .body(DealDto.class);
    }
}
