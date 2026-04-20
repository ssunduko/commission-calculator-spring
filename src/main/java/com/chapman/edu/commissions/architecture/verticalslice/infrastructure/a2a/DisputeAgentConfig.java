package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.a2a;

import io.a2a.server.PublicAgentCard;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * A2A (Agent-to-Agent) server wiring for the dispute-filing agent.
 *
 * <p>When enabled, {@code spring-ai-a2a-server-autoconfigure} exposes:
 * <ul>
 *   <li>{@code GET /.well-known/agent-card.json} — agent discovery</li>
 *   <li>{@code POST /} — JSON-RPC sendMessage</li>
 * </ul>
 *
 * <p><strong>Activation requires all of:</strong>
 * <ol>
 *   <li>{@code spring.ai.a2a.server.enabled=true} (also turns on the Spring AI
 *       A2A server autoconfigure, which otherwise can't find our AgentCard /
 *       AgentExecutor beans and would fail startup).</li>
 *   <li>A live {@code ChatClient.Builder} — meaning the Anthropic auto-config
 *       must be on the classpath and its API key set. The verticalslice app
 *       currently excludes {@code AnthropicChatAutoConfiguration} at
 *       {@code CommissionCalculatorApplication:33}; drop that exclusion (or run
 *       a profile that does) to light up the agent.</li>
 * </ol>
 */
@Configuration
@ConditionalOnProperty(name = "spring.ai.a2a.server.enabled", havingValue = "true")
public class DisputeAgentConfig {

    private static final String SYSTEM_PROMPT = """
        You are the dispute-filing agent for a sales commission platform.

        When another agent asks you to file a dispute you MUST:
          1. Resolve a real calculationId. If the caller gives you one, verify it
             with getCalculation. Otherwise use listCalculations or
             listCalculationsForSalesRep to find a plausible match.
          2. Derive salesRepId from the selected calculation — do not invent it.
          3. Call createDispute with a clear title and description. If the caller
             specifies urgency, translate it into priority (LOW/MEDIUM/HIGH/URGENT).
          4. After creation, respond with the new dispute id, title, priority,
             status, and the underlying calculationId so the caller can confirm.

        Refuse and explain if you cannot find a matching calculation — never
        fabricate ids. Keep answers terse and factual.
        """;

    @Bean
    public ChatClient disputeAgentChatClient(ChatClient.Builder builder, DisputeAgentTools tools) {
        return builder
            .defaultSystem(SYSTEM_PROMPT)
            .defaultTools(tools)
            .build();
    }

    @Bean
    public AgentExecutor disputeAgentExecutor(ChatClient disputeAgentChatClient) {
        return new DefaultAgentExecutor(disputeAgentChatClient, (chat, requestContext) -> {
            String userMessage = DefaultAgentExecutor.extractTextFromMessage(requestContext.getMessage());
            return chat.prompt().user(userMessage).call().content();
        });
    }

    @Bean
    @PublicAgentCard
    public AgentCard disputeAgentCard(@Value("${a2a.dispute-agent.url:http://localhost:8081}") String agentUrl) {
        AgentSkill fileDispute = new AgentSkill.Builder()
            .id("file-dispute")
            .name("File a commission dispute")
            .description("Given a sales rep (or calculation) and a reason, creates a dispute record "
                + "via the platform's DisputeService and returns the new dispute id + status.")
            .tags(List.of("disputes", "commissions", "create"))
            .examples(List.of(
                "File a dispute for sales rep usr-002 about an incorrect 12% rate — they expected 15%.",
                "Open an URGENT dispute on calculation calc-003; the payout was $2,400 but should be $3,000.",
                "Create a dispute for the latest calculation belonging to rep usr-001; title 'Missing bonus'."
            ))
            .inputModes(List.of("text/plain"))
            .outputModes(List.of("text/plain"))
            .build();

        AgentSkill lookupDispute = new AgentSkill.Builder()
            .id("lookup-dispute")
            .name("Look up commission disputes")
            .description("Retrieves existing disputes or a single dispute by id so a calling agent can "
                + "verify a filing succeeded or summarize open issues.")
            .tags(List.of("disputes", "commissions", "read"))
            .examples(List.of(
                "List all open disputes.",
                "Fetch the dispute with id 34611767-3e3b-4aa0-afbc-8b6fcbc34c59."
            ))
            .inputModes(List.of("text/plain"))
            .outputModes(List.of("text/plain"))
            .build();

        AgentCapabilities capabilities = new AgentCapabilities.Builder()
            .streaming(false)
            .pushNotifications(false)
            .stateTransitionHistory(false)
            .build();

        return new AgentCard.Builder()
            .name("Dispute Filing Agent")
            .description("Files commission disputes on behalf of a calling agent. Resolves "
                + "calculation ids, derives sales rep from the calc, and creates the dispute.")
            .url(agentUrl)
            .version("0.1.0")
            .protocolVersion("0.3.0")
            .capabilities(capabilities)
            .defaultInputModes(List.of("text/plain"))
            .defaultOutputModes(List.of("text/plain"))
            .skills(List.of(fileDispute, lookupDispute))
            .build();
    }
}
