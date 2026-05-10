package com.chapman.edu.commissions.ai.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * SPRING AI CONFIGURATION: AiConfig
 * ============================================================
 *
 * CONCEPT: Spring AI Framework Setup and Configuration
 * ------------------------------------------------------------
 * Spring AI is a Spring Framework module that provides a consistent,
 * Spring-idiomatic API for integrating AI models into Spring applications.
 *
 * KEY CONCEPTS:
 *
 * 1. AUTO-CONFIGURATION:
 *    Spring AI uses Spring Boot auto-configuration to register beans
 *    automatically. When you add the `spring-ai-anthropic-spring-boot-starter`
 *    dependency, Spring Boot auto-configures:
 *      - AnthropicChatModel: The model implementation for Claude
 *      - ChatClient.Builder: A fluent builder for creating chat clients
 *
 * 2. ChatModel vs ChatClient:
 *    - ChatModel (e.g., AnthropicChatModel): Low-level interface to the AI model.
 *      Handles raw API calls, token counting, and model-specific parameters.
 *    - ChatClient: Higher-level, fluent API built ON TOP of ChatModel.
 *      Provides prompt templating, system messages, advisors (middleware),
 *      and output parsing. Think of it like RestTemplate vs WebClient.
 *
 * 3. CONFIGURATION PROPERTIES:
 *    Spring AI reads from application.properties:
 *      - spring.ai.anthropic.api-key: Your Anthropic API key
 *      - spring.ai.anthropic.chat.options.model: Model name (claude-sonnet-4-5-20250514)
 *      - spring.ai.anthropic.chat.options.max-tokens: Maximum response tokens
 *      - spring.ai.anthropic.chat.options.temperature: Creativity (0.0-1.0)
 *
 * 4. WHY ChatClient.Builder?
 *    ChatClient.Builder is prototype-scoped by default. This allows each
 *    service to build its own ChatClient with different system prompts,
 *    advisors, and configurations — all sharing the same underlying model.
 *
 * ANALOGY:
 *    ChatModel  ≈ DataSource (raw connection to AI)
 *    ChatClient ≈ JdbcTemplate (convenient wrapper with features)
 */
@Configuration
public class AiConfig {

    /**
     * Creates a ChatClient bean configured for general commission analysis.
     *
     * ChatClient.Builder is auto-configured by Spring AI and injected here.
     * We customize it with a default system prompt that establishes the AI's
     * role as a commission domain expert.
     *
     * SYSTEM PROMPT:
     * The system prompt is sent with EVERY request to the AI model.
     * It establishes the AI's persona, domain knowledge, and behavioral
     * constraints. This is a fundamental prompt engineering technique.
     *
     * @param builder Auto-configured ChatClient.Builder backed by AnthropicChatModel
     * @return A ChatClient instance ready for commission-related queries
     */
    @Bean
    public ChatClient commissionChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are an expert commission calculator assistant for a sales organization.
                        You help analyze commission plans, calculate commissions, explain commission
                        structures, and answer questions about deals and payouts.

                        When analyzing data, be precise with numbers and percentages.
                        Always explain your reasoning step by step.
                        Format currency values with $ and two decimal places.
                        """)
                .build();
    }

    /**
     * Wires Spring AOP into Micrometer's Observation API. Without this bean,
     * @Observed annotations on Spring beans are silently ignored — methods
     * still run, but no spans/timers are produced. With it, every annotated
     * method becomes a span exported to Jaeger via OTLP and a timer scraped
     * by Prometheus.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
