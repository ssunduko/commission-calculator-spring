package com.chapman.edu.commissions.ai.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * STARTUP DEMO: Processor Demonstration Runner
 * ============================================================
 *
 * CONCEPT: Demonstrating AI Capabilities at Application Startup
 * ------------------------------------------------------------
 * This class invokes all four processors during application startup
 * to demonstrate the Spring AI framework capabilities in the context
 * of the Commission Calculator application.
 *
 * EXECUTION ORDER:
 * 1. SampleDataLoader runs first (loads DB + vector store)    [@Order default]
 * 2. ProcessorStartupDemo runs second (demos all processors)  [@Order(2)]
 *
 * DEMO CATEGORIES:
 * - AiProcessor: Framework setup, configuration, ChatClient usage
 * - PromptProcessor: Prompt engineering techniques and templates
 * - SearchProcessor: Vector store semantic search (runs locally)
 * - RagProcessor: Full RAG pipeline (retrieval + generation)
 *
 * NOTE ON AI API CALLS:
 * Methods that call the Claude API are wrapped in try-catch blocks.
 * If the API key is invalid or the service is unreachable, the demo
 * logs a warning and continues. Local operations (search, config)
 * always succeed regardless of API availability.
 */
@Configuration
public class ProcessorStartupDemo {

    private static final Logger log = LoggerFactory.getLogger(ProcessorStartupDemo.class);

    private static final String BANNER = "═".repeat(60);
    private static final String SECTION = "─".repeat(40);

    @Bean
    @Order(2)
    public CommandLineRunner runProcessorDemos(AiProcessor aiProcessor,
                                                PromptProcessor promptProcessor,
                                                SearchProcessor searchProcessor,
                                                RagProcessor ragProcessor) {
        return args -> {
            log.info("\n\n{}", BANNER);
            log.info("  PROCESSOR STARTUP DEMO");
            log.info("  Demonstrating Spring AI Framework Capabilities");
            log.info("{}\n", BANNER);

            demoAiProcessor(aiProcessor);
            demoSearchProcessor(searchProcessor);
            demoPromptProcessor(promptProcessor);
            demoRagProcessor(ragProcessor);

            log.info("\n{}", BANNER);
            log.info("  PROCESSOR STARTUP DEMO COMPLETE");
            log.info("{}\n", BANNER);
        };
    }

    // ============================================================
    // 1. AI PROCESSOR DEMO — Framework Setup & Configuration
    // ============================================================

    private void demoAiProcessor(AiProcessor aiProcessor) {
        log.info("\n{}", SECTION);
        log.info("  [1/4] AiProcessor — Spring AI Framework Setup");
        log.info("{}", SECTION);

        // Demo 1a: Configuration inspection (no API call)
        log.info("\n>>> Demo 1a: AI Configuration Inspection");
        Map<String, Object> config = aiProcessor.getAiConfiguration();
        config.forEach((key, value) -> log.info("    {} = {}", key, value));

        // Demo 1b: Health check (minimal API call)
        log.info("\n>>> Demo 1b: AI Service Health Check");
        try {
            Map<String, Object> health = aiProcessor.healthCheck();
            health.forEach((key, value) -> log.info("    {} = {}", key, value));
        } catch (Exception e) {
            log.warn("    Health check skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 1c: Simple query (API call)
        log.info("\n>>> Demo 1c: Simple Commission Query");
        try {
            String response = aiProcessor.processSimpleQuery(
                    "What is a tiered commission structure? Explain in 2 sentences.");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Simple query skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 1d: Role-based query (API call)
        log.info("\n>>> Demo 1d: Role-Based Analysis (finance auditor)");
        try {
            String response = aiProcessor.processWithSystemContext(
                    "A sales rep earned $37,500 commission on a $250,000 deal. Is this rate normal?",
                    "finance auditor");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Role-based query skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 1e: Commission analysis (API call)
        log.info("\n>>> Demo 1e: Structured Commission Analysis");
        try {
            String response = aiProcessor.analyzeCommission(
                    "Acme Corp Enterprise License", "150000", "12", "18000");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Commission analysis skipped — API unavailable: {}", e.getMessage());
        }
    }

    // ============================================================
    // 2. SEARCH PROCESSOR DEMO — Vector Store & Semantic Search
    // ============================================================

    private void demoSearchProcessor(SearchProcessor searchProcessor) {
        log.info("\n{}", SECTION);
        log.info("  [2/4] SearchProcessor — Vector DB & Semantic Search");
        log.info("{}", SECTION);

        // Demo 2a: Basic semantic search (local — no API call)
        log.info("\n>>> Demo 2a: Basic Semantic Search — 'enterprise deals'");
        List<Map<String, Object>> results = searchProcessor.semanticSearch("enterprise deals", 3);
        logSearchResults(results);

        // Demo 2b: Filtered search by type (local)
        log.info("\n>>> Demo 2b: Filtered Search — type='commission_plan'");
        List<Map<String, Object>> planResults = searchProcessor.filteredSearch(
                "commission rates and tiers", "commission_plan", 3);
        logSearchResults(planResults);

        // Demo 2c: Filtered search for users (local)
        log.info("\n>>> Demo 2c: Filtered Search — type='user'");
        List<Map<String, Object>> userResults = searchProcessor.filteredSearch(
                "sales representatives on the team", "user", 3);
        logSearchResults(userResults);

        // Demo 2d: Comparative search — semantic equivalence (local)
        log.info("\n>>> Demo 2d: Comparative Search — Semantic Equivalence");
        Map<String, Object> comparison = searchProcessor.compareSearchResults(
                "high value deals", "large enterprise contracts", 3);
        log.info("    Query 1: '{}' → {} results", comparison.get("query1"), comparison.get("query1Count"));
        log.info("    Query 2: '{}' → {} results", comparison.get("query2"), comparison.get("query2Count"));
        log.info("    Overlapping documents: {}", comparison.get("overlappingDocuments"));
        log.info("    Conclusion: {}", comparison.get("semanticSimilarityDemonstrated"));

        // Demo 2e: Context extraction for RAG (local)
        log.info("\n>>> Demo 2e: Context Extraction for RAG Pipeline");
        String context = searchProcessor.extractSearchContext("commission calculations and payouts", 3);
        log.info("    Extracted context:\n{}", indent(context));

        // Demo 2f: Custom search with tuned parameters (local)
        log.info("\n>>> Demo 2f: Custom Search — Low Threshold (0.3)");
        List<Map<String, Object>> customResults = searchProcessor.customSearch(
                "quarterly bonus accelerator", 5, 0.3, null);
        logSearchResults(customResults);
    }

    // ============================================================
    // 3. PROMPT PROCESSOR DEMO — Prompt Engineering Techniques
    // ============================================================

    private void demoPromptProcessor(PromptProcessor promptProcessor) {
        log.info("\n{}", SECTION);
        log.info("  [3/4] PromptProcessor — Prompt Engineering");
        log.info("{}", SECTION);

        // Demo 3a: Role assignment (API call)
        log.info("\n>>> Demo 3a: Role Assignment — Senior Commission Analyst");
        try {
            String response = promptProcessor.processWithRoleAssignment(
                    "Evaluate whether a 12% commission rate on a $150,000 enterprise deal is competitive.");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Role assignment demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 3b: Chain-of-thought (API call)
        log.info("\n>>> Demo 3b: Chain-of-Thought — Step-by-Step Calculation");
        try {
            String response = promptProcessor.processWithChainOfThought(
                    "150000",
                    """
                    Standard Sales Plan:
                    - Starter: $0–$25,000 at 5%
                    - Growth: $25,000–$75,000 at 8%
                    - Enterprise: $75,000–$200,000 at 12%
                    - Strategic: $200,000+ at 15%
                    Q1 Accelerator Bonus: 10% on all commissions
                    """);
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Chain-of-thought demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 3c: Few-shot prompting (API call)
        log.info("\n>>> Demo 3c: Few-Shot — Tier Classification by Example");
        try {
            String response = promptProcessor.processWithFewShot(
                    "Global Industries Platform", "250000");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Few-shot demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 3d: Structured output (API call)
        log.info("\n>>> Demo 3d: Structured Output — Dispute Assessment");
        try {
            String response = promptProcessor.processWithStructuredOutput(
                    "Incorrect Tier Rate Applied",
                    "I believe my $150K deal should have been calculated at the Strategic tier (15%) not Enterprise (12%).",
                    "18000",
                    "150000");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Structured output demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 3e: Template-based processing (API call)
        log.info("\n>>> Demo 3e: Template-Based — Commission Analysis (.st file)");
        try {
            String response = promptProcessor.processWithTemplate(
                    "Acme Corp Enterprise License", "150000",
                    "Alice Johnson", "WON",
                    "Standard Sales Plan", "12", "18000");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Template demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 3f: Dynamic template (API call)
        log.info("\n>>> Demo 3f: Dynamic Template — Adaptive Prompt Construction");
        try {
            String response = promptProcessor.processWithDynamicTemplate(Map.of(
                    "salesRep", "Alice Johnson",
                    "dealTitle", "Acme Corp Enterprise License",
                    "dealValue", "150000",
                    "commissionPlan", "Standard Sales Plan",
                    "commissionAmount", "18000"
            ));
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Dynamic template demo skipped — API unavailable: {}", e.getMessage());
        }
    }

    // ============================================================
    // 4. RAG PROCESSOR DEMO — Retrieval-Augmented Generation
    // ============================================================

    private void demoRagProcessor(RagProcessor ragProcessor) {
        log.info("\n{}", SECTION);
        log.info("  [4/4] RagProcessor — RAG Implementation");
        log.info("{}", SECTION);

        // Demo 4a: Basic RAG query (API call)
        log.info("\n>>> Demo 4a: Basic RAG — Question about Commission Plans");
        try {
            String response = ragProcessor.processRagQuery(
                    "What commission plans are available and what are the tier rates?");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Basic RAG demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 4b: Filtered RAG (API call)
        log.info("\n>>> Demo 4b: Filtered RAG — Deals Only");
        try {
            String response = ragProcessor.processFilteredRagQuery(
                    "What are the highest value deals?", "deal");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Filtered RAG demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 4c: Multi-retrieval RAG (API call)
        log.info("\n>>> Demo 4c: Multi-Retrieval RAG — Performance Report");
        try {
            String response = ragProcessor.processMultiRetrievalRag("Alice");
            log.info("    Response: {}", truncate(response));
        } catch (Exception e) {
            log.warn("    Multi-retrieval RAG demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 4d: RAG with explicit steps (API call)
        log.info("\n>>> Demo 4d: RAG Pipeline — Explicit Stages");
        try {
            Map<String, Object> pipeline = ragProcessor.processRagWithExplicitSteps(
                    "How much commission did the enterprise deals generate?", 3);

            log.info("    Question: {}", pipeline.get("question"));

            @SuppressWarnings("unchecked")
            Map<String, Object> retrieval = (Map<String, Object>) pipeline.get("stage1_retrieval");
            log.info("    Stage 1 (Retrieval): {} documents retrieved", retrieval.get("documentsRetrieved"));

            @SuppressWarnings("unchecked")
            Map<String, Object> augmentation = (Map<String, Object>) pipeline.get("stage2_augmentation");
            log.info("    Stage 2 (Augmentation): Context injected into prompt");
            log.info("      Context preview: {}", truncate((String) augmentation.get("contextInjected")));

            @SuppressWarnings("unchecked")
            Map<String, Object> generation = (Map<String, Object>) pipeline.get("stage3_generation");
            log.info("    Stage 3 (Generation): {}", truncate((String) generation.get("answer")));
        } catch (Exception e) {
            log.warn("    RAG pipeline demo skipped — API unavailable: {}", e.getMessage());
        }

        // Demo 4e: RAG vs Direct comparison (API call)
        log.info("\n>>> Demo 4e: RAG vs Direct — Grounded vs Ungrounded Answers");
        try {
            Map<String, Object> comparison = ragProcessor.compareRagVsDirect(
                    "What commission rate applies to a $150,000 deal?");

            @SuppressWarnings("unchecked")
            Map<String, Object> rag = (Map<String, Object>) comparison.get("ragAnswer");
            @SuppressWarnings("unchecked")
            Map<String, Object> direct = (Map<String, Object>) comparison.get("directAnswer");

            log.info("    RAG Answer (grounded, {} docs): {}", rag.get("documentsUsed"),
                    truncate((String) rag.get("response")));
            log.info("    Direct Answer (ungrounded): {}",
                    truncate((String) direct.get("response")));
            log.info("    Recommendation: {}", comparison.get("recommendation"));
        } catch (Exception e) {
            log.warn("    RAG vs Direct demo skipped — API unavailable: {}", e.getMessage());
        }
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private void logSearchResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            log.info("    No results found");
            return;
        }
        for (Map<String, Object> result : results) {
            String content = (String) result.get("content");
            log.info("    [{}] {}", result.get("rank"), truncate(content));
        }
    }

    private String truncate(String text) {
        if (text == null) return "(null)";
        String oneLine = text.replace("\n", " ").replace("\r", "").trim();
        if (oneLine.length() <= 200) return oneLine;
        return oneLine.substring(0, 200) + "...";
    }

    private String indent(String text) {
        if (text == null) return "      (null)";
        return text.lines()
                .map(line -> "      " + line)
                .reduce("", (a, b) -> a + "\n" + b)
                .trim();
    }
}
