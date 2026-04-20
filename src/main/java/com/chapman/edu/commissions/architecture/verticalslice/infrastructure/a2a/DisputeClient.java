package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a;

import io.a2a.client.Client;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfigBuilder;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A2A client that talks to the local dispute-filing agent over JSON-RPC.
 * Exposes {@link #sendTask(String)} — called by the CLI runner and the REST
 * controller to forward a natural-language task to the agent.
 *
 * <p>Deliberately has no {@code @Tool} methods: when server and client live in
 * the same JVM, exposing a tool here would let the server's own ChatClient
 * pick it and recursively POST to itself.
 *
 * <p>We construct a minimal {@link AgentCard} from config rather than fetching
 * {@code /.well-known/agent-card.json} — the JSON-RPC transport only needs a
 * URL and a name to target. For production you'd fetch the real card first.
 */
@Component
public class DisputeClient {

    private static final Logger log = LoggerFactory.getLogger(DisputeClient.class);

    private final String agentUrl;

    public DisputeClient(@Value("${a2a.dispute-agent.url:http://localhost:8081}") String agentUrl) {
        this.agentUrl = agentUrl;
    }

    /**
     * Sends {@code task} to the remote agent and blocks until a terminal event
     * arrives (or the 180s timeout fires). Returns the agent's reply text.
     *
     * <p>Handles all three {@code ClientEvent} shapes the A2A server may emit:
     * <ul>
     *   <li>{@link MessageEvent} — immediate synchronous reply.</li>
     *   <li>{@link TaskEvent} — a {@link Task} carrying status message + artifacts.</li>
     *   <li>{@link TaskUpdateEvent} — task state transitions; we wait for a final
     *       state ({@code COMPLETED} / {@code FAILED} / {@code REJECTED} / {@code CANCELED}).</li>
     * </ul>
     */
    public String sendTask(String task) {
        CompletableFuture<String> reply = new CompletableFuture<>();
        AgentCard card = minimalRemoteCard(agentUrl);
        Client client = null;
        try {
            client = Client.builder(card)
                .addConsumer((event, agentCard) -> {
                    log.debug("A2A event: {}", event.getClass().getSimpleName());
                    if (event instanceof MessageEvent me) {
                        reply.complete(extractText(me.getMessage()));
                    } else if (event instanceof TaskEvent te) {
                        completeIfFinal(reply, te.getTask());
                    } else if (event instanceof TaskUpdateEvent tue) {
                        completeIfFinal(reply, tue.getTask());
                    }
                })
                .streamingErrorHandler(err -> {
                    log.warn("A2A stream error", err);
                    reply.completeExceptionally(err);
                })
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfigBuilder())
                .build();

            Message message = new Message.Builder()
                .role(Message.Role.USER)
                .parts(new TextPart(task))
                .messageId(UUID.randomUUID().toString())
                .build();

            log.info("A2A -> {} : {}", agentUrl, task);
            client.sendMessage(message);
            return reply.get(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("A2A call failed", e);
            return "A2A call failed: " + e.getMessage();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignore) {
                    // best-effort close
                }
            }
        }
    }

    /**
     * Completes {@code reply} only when {@code task} has reached a terminal
     * {@code TaskState} — otherwise intermediate progress events would settle
     * the future prematurely and we'd return an empty string.
     */
    private static void completeIfFinal(CompletableFuture<String> reply, Task task) {
        if (task == null || reply.isDone()) {
            return;
        }
        TaskStatus status = task.getStatus();
        if (status == null || status.state() == null || !status.state().isFinal()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (status.message() != null) {
            sb.append(extractText(status.message()));
        }
        if (task.getArtifacts() != null) {
            for (Artifact a : task.getArtifacts()) {
                if (a.parts() != null) {
                    for (Part<?> p : a.parts()) {
                        if (p instanceof TextPart tp) {
                            if (sb.length() > 0) {
                                sb.append('\n');
                            }
                            sb.append(tp.getText());
                        }
                    }
                }
            }
        }
        if (sb.length() == 0) {
            sb.append("(task ").append(status.state().name()).append(" with no text)");
        }
        reply.complete(sb.toString());
    }

    private static String extractText(Message message) {
        if (message == null || message.getParts() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Part<?> p : message.getParts()) {
            if (p instanceof TextPart tp) {
                sb.append(tp.getText());
            }
        }
        return sb.toString();
    }

    /**
     * Enough to build a JSON-RPC transport pointed at {@code url}. Skills are
     * empty because the transport only consults {@code url} and transport prefs.
     */
    private static AgentCard minimalRemoteCard(String url) {
        AgentCapabilities caps = new AgentCapabilities.Builder()
            .streaming(false)
            .pushNotifications(false)
            .stateTransitionHistory(false)
            .build();
        return new AgentCard.Builder()
            .name("Dispute Filing Agent (remote stub)")
            .description("Stub card — we only need url+transport for the client.")
            .url(url)
            .version("0.0.0")
            .protocolVersion("0.3.0")
            .capabilities(caps)
            .defaultInputModes(List.of("text/plain"))
            .defaultOutputModes(List.of("text/plain"))
            .skills(List.of())
            .build();
    }
}
