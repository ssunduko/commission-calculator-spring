package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.rag.CommissionRagService;
import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ============================================================
 * PROCESSOR: RagProcessor
 * ============================================================
 *
 * CONCEPT: RAG (Retrieval-Augmented Generation) Implementation
 * ------------------------------------------------------------
 * This processor demonstrates the RAG pattern — the most important
 * technique for building enterprise AI applications that need to
 * answer questions about YOUR data, not just general knowledge.
 *
 * THE RAG PROBLEM:
 * AI models like Claude have vast general knowledge but know NOTHING about:
 * - Your company's commission plans and tier rates
 * - Your sales team's deal history and performance
 * - Your specific business rules and calculation logic
 *
 * THE RAG SOLUTION:
 * Instead of retraining the model (expensive, slow), we RETRIEVE relevant
 * documents from our database and INJECT them into the prompt as context.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                    THE RAG PIPELINE                               │
 * │                                                                   │
 * │  ┌─────────┐     ┌──────────────┐     ┌──────────────────────┐   │
 * │  │  USER    │     │  RETRIEVAL   │     │    AUGMENTATION      │   │
 * │  │ QUESTION │────▶│              │────▶│                      │   │
 * │  │          │     │ Question     │     │ System Prompt        │   │
 * │  └─────────┘     │ → Embedding  │     │ + Retrieved Context  │   │
 * │                   │ → Vector     │     │ + User Question      │   │
 * │                   │   Search     │     │ = Augmented Prompt   │   │
 * │                   │ → Top-K Docs │     │                      │   │
 * │                   └──────────────┘     └──────────┬───────────┘   │
 * │                                                    │              │
 * │                                        ┌───────────▼───────────┐  │
 * │                                        │    GENERATION         │  │
 * │                                        │                       │  │
 * │                                        │ Augmented Prompt      │  │
 * │                                        │ → Claude API          │  │
 * │                                        │ → Grounded Answer     │  │
 * │                                        └───────────────────────┘  │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * RAG VARIANTS DEMONSTRATED:
 *
 * 1. BASIC RAG:
 *    Single retrieval → single prompt → single answer
 *    "What commission plans are available?" → search all docs → answer
 *
 * 2. FILTERED RAG:
 *    Type-filtered retrieval → focused prompt → focused answer
 *    "Tell me about deals" → search only deal docs → answer
 *
 * 3. MULTI-RETRIEVAL RAG:
 *    Multiple retrievals (deals + plans + calcs) → combined context → rich answer
 *    "Analyze Alice's performance" → search user + deals + calcs + plans → report
 *
 * 4. RAG WITH EXPLICIT STEPS:
 *    Shows each pipeline stage separately for educational purposes
 *
 * RAG vs FINE-TUNING vs PROMPT ENGINEERING:
 * - RAG: Best for factual Q&A over dynamic data (our commission data)
 * - Fine-tuning: Best for teaching new skills/behaviors (expensive, static)
 * - Prompt Engineering: Best for formatting and reasoning guidance
 * - In practice: Combine all three (RAG + prompt engineering is most common)
 */
@Service
public class RagProcessor {

    private static final Logger log = LoggerFactory.getLogger(RagProcessor.class);

    private final ChatClient chatClient;
    private final CommissionRagService ragService;
    private final EmbeddingSearchService searchService;

    public RagProcessor(ChatClient commissionChatClient,
                        CommissionRagService ragService,
                        EmbeddingSearchService searchService) {
        this.chatClient = commissionChatClient;
        this.ragService = ragService;
        this.searchService = searchService;
    }

    // ============================================================
    // 1. BASIC RAG — Standard Question Answering
    // ============================================================

    /**
     * Demonstrates the standard RAG pipeline for commission Q&A.
     *
     * BASIC RAG FLOW:
     * 1. RETRIEVAL: User question → vector search → top 5 similar documents
     * 2. AUGMENTATION: System prompt + retrieved context + question
     * 3. GENERATION: Augmented prompt → Claude → grounded answer
     *
     * GROUNDING:
     * The AI's answer is "grounded" in the retrieved documents, meaning
     * it can only use information from your database. This prevents
     * hallucination (the AI making up facts about your commission data).
     *
     * EXAMPLE:
     * Q: "What commission plans are available?"
     * Retrieval: Finds document about "Standard Sales Plan with 4 tiers..."
     * Answer: "The Standard Sales Plan is available with tiers: Starter (5%),
     *          Growth (8%), Enterprise (12%), and Strategic (15%)."
     *
     * @param question A natural language question about commissions
     * @return An AI-generated answer grounded in commission data
     */
    public String processRagQuery(String question) {
        log.info("Processing RAG query: '{}'", question);

        String answer = ragService.answerQuestion(question);

        log.info("RAG query processed successfully");
        return answer;
    }

    // ============================================================
    // 2. FILTERED RAG — Type-Specific Retrieval
    // ============================================================

    /**
     * Demonstrates filtered RAG with document type constraints.
     *
     * FILTERED RAG:
     * Narrows the retrieval stage to documents of a specific type.
     * This improves both precision (fewer irrelevant results) and
     * response quality (AI focuses on the right domain).
     *
     * WHEN TO USE FILTERED vs BASIC RAG:
     * - Basic: "Tell me about Alice's commissions" → search everything
     * - Filtered: "What deals are in the pipeline?" → search only deals
     *
     * FILTER TYPES IN COMMISSION CALCULATOR:
     * - "deal": Questions about specific deals, values, statuses
     * - "commission_plan": Questions about plan structures, rates, tiers
     * - "commission_calculation": Questions about calculated amounts
     * - "user": Questions about sales team members
     *
     * @param question     The user's question
     * @param documentType The type of document to search
     * @return An AI answer filtered to the specified domain
     */
    public String processFilteredRagQuery(String question, String documentType) {
        log.info("Processing filtered RAG query: '{}' (type={})", question, documentType);

        String answer = ragService.answerTypedQuestion(question, documentType);

        log.info("Filtered RAG query processed for type: {}", documentType);
        return answer;
    }

    // ============================================================
    // 3. MULTI-RETRIEVAL RAG — Comprehensive Analysis
    // ============================================================

    /**
     * Demonstrates multi-retrieval RAG for comprehensive reporting.
     *
     * MULTI-RETRIEVAL RAG:
     * Instead of a single retrieval, this pattern makes MULTIPLE
     * retrieval queries across different document types, then combines
     * all retrieved contexts into a single rich prompt.
     *
     * WHY MULTI-RETRIEVAL?
     * A performance report for a sales rep needs data from:
     * - User documents: Rep's profile, department, territory
     * - Deal documents: Their deals, values, statuses
     * - Calculation documents: Their commission amounts and history
     * - Plan documents: The plan rules that govern their commissions
     *
     * No single retrieval query could capture all of this context
     * effectively. Multi-retrieval ensures comprehensive coverage.
     *
     * CONTEXT STRUCTURE:
     * === Sales Representative Info ===
     * [user doc results]
     * === Deals ===
     * [deal doc results]
     * === Commission Calculations ===
     * [calculation doc results]
     * === Commission Plans ===
     * [plan doc results]
     *
     * @param salesRepName The sales representative's name
     * @return A comprehensive AI-generated performance report
     */
    public String processMultiRetrievalRag(String salesRepName) {
        log.info("Processing multi-retrieval RAG for: {}", salesRepName);

        String report = ragService.generatePerformanceReport(salesRepName);

        log.info("Multi-retrieval RAG report generated for: {}", salesRepName);
        return report;
    }

    // ============================================================
    // 4. RAG WITH EXPLICIT STEPS — Educational Pipeline View
    // ============================================================

    /**
     * Demonstrates each RAG pipeline stage explicitly, returning
     * intermediate results for educational inspection.
     *
     * This method breaks the RAG pipeline into visible steps so you can
     * see exactly what happens at each stage:
     *
     * STAGE 1 — RETRIEVAL:
     * - User question → embedding model → query vector
     * - Query vector → vector store → cosine similarity search
     * - Returns top-K most similar documents
     * - OUTPUT: List of retrieved documents with content and metadata
     *
     * STAGE 2 — AUGMENTATION:
     * - System prompt: Establishes AI role and constraints
     * - Retrieved context: Formatted text from retrieved documents
     * - User question: The original question
     * - Combined into a single augmented prompt
     * - OUTPUT: The complete prompt that will be sent to the AI
     *
     * STAGE 3 — GENERATION:
     * - Augmented prompt → Claude API → AI-generated response
     * - Response is grounded in the retrieved context
     * - OUTPUT: The final answer
     *
     * @param question A natural language question about commissions
     * @param topK     Number of documents to retrieve
     * @return A map with results from each pipeline stage
     */
    public Map<String, Object> processRagWithExplicitSteps(String question, int topK) {
        log.info("Processing RAG with explicit steps: '{}' (topK={})", question, topK);

        Map<String, Object> pipeline = new LinkedHashMap<>();
        pipeline.put("question", question);

        // ================================================================
        // STAGE 1: RETRIEVAL
        // ================================================================
        // Convert question to vector, search vector store, get top-K docs
        List<Document> retrievedDocs = searchService.search(question, topK);

        List<Map<String, Object>> retrievalResults = new ArrayList<>();
        for (int i = 0; i < retrievedDocs.size(); i++) {
            Document doc = retrievedDocs.get(i);
            Map<String, Object> docInfo = new LinkedHashMap<>();
            docInfo.put("rank", i + 1);
            docInfo.put("content", doc.getText());
            docInfo.put("metadata", doc.getMetadata());
            retrievalResults.add(docInfo);
        }

        pipeline.put("stage1_retrieval", Map.of(
                "description", "Semantic search converts the question to a vector " +
                        "and finds the most similar documents in the vector store",
                "documentsRetrieved", retrievedDocs.size(),
                "topK", topK,
                "results", retrievalResults
        ));

        // ================================================================
        // STAGE 2: AUGMENTATION
        // ================================================================
        // Format retrieved documents into context and build the prompt
        String context = searchService.extractContext(retrievedDocs);

        String systemPrompt = """
                You are a commission calculation expert assistant.
                Use ONLY the provided context to answer the question.
                If the context doesn't contain enough information, say so clearly.
                Always cite which pieces of context you used in your answer.
                Be precise with numbers and format currency as $X,XXX.XX.
                """;

        String userPrompt = String.format("""
                Context from our commission database:
                %s

                Question: %s

                Please answer based on the context above.
                """, context, question);

        pipeline.put("stage2_augmentation", Map.of(
                "description", "Retrieved documents are formatted as context and combined " +
                        "with the system prompt and user question into an augmented prompt",
                "systemPrompt", systemPrompt.trim(),
                "contextInjected", context,
                "userPromptWithContext", userPrompt.trim()
        ));

        // ================================================================
        // STAGE 3: GENERATION
        // ================================================================
        // Send augmented prompt to Claude and get the grounded answer
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        pipeline.put("stage3_generation", Map.of(
                "description", "The augmented prompt is sent to Claude, which generates " +
                        "a response grounded in the retrieved context",
                "answer", answer
        ));

        log.info("RAG explicit pipeline completed for: '{}'", question);
        return pipeline;
    }

    // ============================================================
    // 5. RAG vs DIRECT — Comparing Grounded vs Ungrounded Answers
    // ============================================================

    /**
     * Demonstrates the difference between RAG-augmented and direct AI queries.
     *
     * RAG (Grounded):
     * - Retrieves relevant commission data from the vector store
     * - Injects that data as context in the prompt
     * - AI generates an answer BASED ON your actual data
     * - Result: Accurate, specific answers about YOUR commissions
     *
     * Direct (Ungrounded):
     * - No data retrieval
     * - AI relies solely on its general training knowledge
     * - Result: Generic answers, likely hallucinated details
     *
     * EXAMPLE COMPARISON:
     * Question: "What commission rate applies to a $150K deal?"
     *
     * RAG Answer: "Based on the Standard Sales Plan, a $150K deal falls
     *             in the Enterprise tier ($75K–$200K) at 12% commission."
     *             (Accurate — grounded in your actual plan data)
     *
     * Direct Answer: "Typically 5-15% depending on the company..."
     *                (Generic — doesn't know your specific tiers)
     *
     * @param question A question about commissions
     * @return A map comparing RAG and direct responses
     */
    public Map<String, Object> compareRagVsDirect(String question) {
        log.info("Comparing RAG vs direct for: '{}'", question);

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("question", question);

        // RAG-augmented answer (uses your commission data)
        List<Document> docs = searchService.search(question, 5);
        String context = searchService.extractContext(docs);

        String ragAnswer = chatClient.prompt()
                .system("""
                        You are a commission expert. Use ONLY the provided context
                        to answer. If the context doesn't help, say so.
                        """)
                .user(String.format("""
                        Context from commission database:
                        %s

                        Question: %s
                        """, context, question))
                .call()
                .content();

        comparison.put("ragAnswer", Map.of(
                "response", ragAnswer,
                "documentsUsed", docs.size(),
                "grounded", true,
                "description", "Answer is grounded in actual commission data " +
                        "retrieved from the vector store"
        ));

        // Direct answer (no retrieval, just general AI knowledge)
        String directAnswer = chatClient.prompt()
                .system("You are a commission expert. Answer based on your general knowledge.")
                .user(question)
                .call()
                .content();

        comparison.put("directAnswer", Map.of(
                "response", directAnswer,
                "documentsUsed", 0,
                "grounded", false,
                "description", "Answer uses only the AI's general training knowledge. " +
                        "May contain hallucinated details about your specific data."
        ));

        comparison.put("recommendation",
                "Always prefer RAG for questions about your specific commission data. " +
                "Use direct queries only for general knowledge questions.");

        log.info("RAG vs direct comparison completed");
        return comparison;
    }

    // ============================================================
    // 6. CONTEXTUAL FOLLOW-UP RAG
    // ============================================================

    /**
     * Demonstrates RAG with conversational follow-up capability.
     *
     * CONVERSATIONAL RAG:
     * In a real application, users ask follow-up questions:
     * - Q1: "What deals has Alice closed?"
     * - Q2: "How much commission did she earn from those?"
     *
     * Q2 needs context from Q1's retrieval to be meaningful.
     * This method takes a prior context and performs a follow-up
     * RAG query that combines both the original and new context.
     *
     * @param originalQuestion The original question
     * @param originalAnswer   The AI's previous answer
     * @param followUpQuestion The follow-up question
     * @return An AI answer that considers both the original and new context
     */
    public String processRagFollowUp(String originalQuestion, String originalAnswer,
                                      String followUpQuestion) {
        log.info("Processing RAG follow-up: '{}' → '{}'", originalQuestion, followUpQuestion);

        // Retrieve new context relevant to the follow-up
        List<Document> newDocs = searchService.search(followUpQuestion, 5);
        String newContext = searchService.extractContext(newDocs);

        String answer = chatClient.prompt()
                .system("""
                        You are a commission expert assistant in a conversation.
                        The user previously asked a question and received an answer.
                        Now they have a follow-up question. Use both the previous
                        conversation context AND the newly retrieved data to answer.
                        """)
                .user(String.format("""
                        Previous Question: %s
                        Previous Answer: %s

                        New context from commission database:
                        %s

                        Follow-up Question: %s
                        """, originalQuestion, originalAnswer, newContext, followUpQuestion))
                .call()
                .content();

        log.info("RAG follow-up processed successfully");
        return answer;
    }
}
