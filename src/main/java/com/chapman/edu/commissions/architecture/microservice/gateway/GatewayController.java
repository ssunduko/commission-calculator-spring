package com.chapman.edu.commissions.architecture.microservice.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * CONCEPT: API Gateway Controller
 *
 * Routes incoming requests to the appropriate microservice based on
 * the URL path prefix. This is a simplified gateway -- production
 * gateways use Spring Cloud Gateway or similar.
 */
@RestController
@RequestMapping("/api/ms")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final RestClient dealClient;
    private final RestClient planClient;
    private final RestClient calculationClient;
    private final RestClient disputeClient;

    public GatewayController(ServiceRegistry registry) {
        this.dealClient = RestClient.builder().baseUrl(registry.getDealServiceUrl()).build();
        this.planClient = RestClient.builder().baseUrl(registry.getPlanServiceUrl()).build();
        this.calculationClient = RestClient.builder().baseUrl(registry.getCalculationServiceUrl()).build();
        this.disputeClient = RestClient.builder().baseUrl(registry.getDisputeServiceUrl()).build();
    }

    @RequestMapping(value = "/deals/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyDeals(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxy(dealClient, request, "/api/ms/deals", "/api/deals", body);
    }

    @RequestMapping(value = "/plans/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyPlans(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxy(planClient, request, "/api/ms/plans", "/api/plans", body);
    }

    @RequestMapping(value = "/calculations/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyCalculations(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxy(calculationClient, request, "/api/ms/calculations", "/api/calculations", body);
    }

    @RequestMapping(value = "/disputes/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyDisputes(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxy(disputeClient, request, "/api/ms/disputes", "/api/disputes", body);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "gateway", "UP",
            "dealService", "http://localhost:8091",
            "planService", "http://localhost:8092",
            "calculationService", "http://localhost:8093",
            "disputeService", "http://localhost:8094"
        ));
    }

    private ResponseEntity<String> proxy(RestClient client, HttpServletRequest request,
                                          String gatewayPrefix, String servicePrefix, String body) {
        String path = request.getRequestURI().replace(gatewayPrefix, servicePrefix);
        String query = request.getQueryString();
        String fullPath = query != null ? path + "?" + query : path;
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.info("[GATEWAY] {} {} -> {}", method, request.getRequestURI(), fullPath);

        try {
            var spec = client.method(method).uri(fullPath);
            if (body != null && !body.isBlank()) {
                spec.header("Content-Type", "application/json").body(body);
            }
            ResponseEntity<String> response = spec.retrieve().toEntity(String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("[GATEWAY] Error proxying to {}: {}", fullPath, e.getMessage());
            return ResponseEntity.status(502).body("{\"error\": \"Service unavailable: " + e.getMessage() + "\"}");
        }
    }
}
