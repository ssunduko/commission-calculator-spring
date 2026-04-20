package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Tiny CLI that fires a single A2A call and prints the response.
 *
 * <p>Listens for {@link ApplicationReadyEvent} — which fires after every
 * {@code CommandLineRunner} / {@code ApplicationRunner} has completed — so
 * the DataInitializer has seeded the in-memory DB before the agent sees the
 * task and can actually resolve calculationId / salesRepId values.
 *
 * <p>Activated only when {@code a2a.client.cli.task} is supplied.
 *
 * <pre>
 *   mvnw -Pverticalslice spring-boot:run \
 *     "-Dspring-boot.run.arguments=--a2a.client.cli.task=File an URGENT dispute for rep usr-002: rate was 12%, should be 15%."
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "a2a.client.cli.task")
public class DisputeAgentCli {

    private static final Logger log = LoggerFactory.getLogger(DisputeAgentCli.class);

    private final DisputeClient disputeClient;
    private final String task;

    public DisputeAgentCli(DisputeClient disputeClient,
                           @Value("${a2a.client.cli.task}") String task) {
        this.disputeClient = disputeClient;
        this.task = task;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("==== A2A CLI: sending task (app fully ready, DB initialized) ====");
        String response = disputeClient.sendTask(task);
        // Direct stdout so the reply is easy to capture in shell pipelines.
        System.out.println();
        System.out.println("==== Dispute agent response ====");
        System.out.println(response);
        System.out.println("================================");
    }
}
