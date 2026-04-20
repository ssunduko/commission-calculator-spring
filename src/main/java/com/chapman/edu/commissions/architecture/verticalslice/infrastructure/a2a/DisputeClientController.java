package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * On-demand trigger for the A2A client half of the app.
 *
 * <p>Same JVM as the dispute agent server, so a single {@code spring-boot:run}
 * stands up both sides. Call this endpoint to make the A2A client open a
 * connection back to the local agent (or any agent configured in
 * {@code a2a.dispute-agent.url}) and forward a natural-language task.
 *
 * <p>Example:
 * <pre>
 *   curl -u admin:admin123 -X POST http://localhost:8081/a2a-client/send \
 *        -H "Content-Type: text/plain" \
 *        --data "File an URGENT dispute for rep usr-002: rate was 12%, should be 15%."
 * </pre>
 */
@RestController
@RequestMapping("/a2a-client")
public class DisputeClientController {

    private final DisputeClient disputeClient;

    public DisputeClientController(DisputeClient disputeClient) {
        this.disputeClient = disputeClient;
    }

    @PostMapping(path = "/send", consumes = {"text/plain", "application/x-www-form-urlencoded", "*/*"})
    public String send(@RequestBody String task) {
        if (task == null || task.isBlank()) {
            return "Error: request body must contain the natural-language task as text/plain.";
        }
        return disputeClient.sendTask(task.trim());
    }
}
