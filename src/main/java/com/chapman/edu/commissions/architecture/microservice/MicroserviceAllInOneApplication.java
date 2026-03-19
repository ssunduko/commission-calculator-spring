package com.chapman.edu.commissions.architecture.microservice;

import com.chapman.edu.commissions.architecture.microservice.calculationservice.CalculationServiceApplication;
import com.chapman.edu.commissions.architecture.microservice.dealservice.DealServiceApplication;
import com.chapman.edu.commissions.architecture.microservice.disputeservice.DisputeServiceApplication;
import com.chapman.edu.commissions.architecture.microservice.gateway.GatewayApplication;
import com.chapman.edu.commissions.architecture.microservice.planservice.PlanServiceApplication;

/**
 * ============================================================
 * ALL-IN-ONE MICROSERVICE STARTER
 * ============================================================
 *
 * Launches all microservices in a single JVM, each in its own
 * Spring ApplicationContext on its own port — just like production,
 * but without needing five separate processes.
 *
 *   Service              Port
 *   ──────────────────── ─────
 *   API Gateway          8090
 *   Deal Service         8091
 *   Plan Service         8092
 *   Calculation Service  8093
 *   Dispute Service      8094
 *
 * Each service has its own database, security config, and
 * component scan — fully isolated contexts.
 *
 * The Gateway proxies requests to the backend services:
 *   /api/ms/deals/**         → Deal Service     (8091)
 *   /api/ms/plans/**         → Plan Service     (8092)
 *   /api/ms/calculations/**  → Calculation Service (8093)
 *   /api/ms/disputes/**      → Dispute Service  (8094)
 */
public class MicroserviceAllInOneApplication {

    public static void main(String[] args) {
        System.out.println("""
            ╔══════════════════════════════════════════════════╗
            ║   MICROSERVICE ALL-IN-ONE LAUNCHER               ║
            ║   Starting all services in a single JVM...       ║
            ╚══════════════════════════════════════════════════╝
            """);

        // Start backend services first, then the gateway
        startInThread("Deal Service (8091)",        () -> DealServiceApplication.main(args));
        startInThread("Plan Service (8092)",        () -> PlanServiceApplication.main(args));
        startInThread("Dispute Service (8094)",     () -> DisputeServiceApplication.main(args));
        startInThread("Calculation Service (8093)", () -> CalculationServiceApplication.main(args));
        startInThread("API Gateway (8090)",         () -> GatewayApplication.main(args));
    }

    private static void startInThread(String serviceName, Runnable starter) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("▶ Starting " + serviceName + "...");
                starter.run();
                System.out.println("✓ " + serviceName + " started successfully");
            } catch (Exception e) {
                System.err.println("✗ " + serviceName + " failed to start: " + e.getMessage());
            }
        }, serviceName);
        thread.setDaemon(false);
        thread.start();
    }
}
