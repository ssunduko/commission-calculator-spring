package com.chapman.edu.commissions.ai.service.rag;

import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================
 * SPRING AI SERVICE: CommissionRagService
 * ============================================================
 *
 * CONCEPT: RAG (Retrieval-Augmented Generation) Implementation
 * ------------------------------------------------------------
 * RAG is a technique that enhances AI responses by providing relevant
 * context retrieved from a knowledge base. It bridges the gap between
 * an AI model's general knowledge and your application's specific data.
 *
 * WHY RAG?
 * AI models like Claude have vast general knowledge but don't know about:
 * - Your specific commission plans and rates
 * - Your sales team's deal history
 * - Your company's business rules
 *
 * RAG solves this by retrieving relevant information from your database
 * and including it in the prompt, so the AI can answer questions about
 * YOUR data accurately.
 *
 * THE RAG PIPELINE (3 Stages):
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ Stage 1: RETRIEVAL                                          │
 * │   User Question → Embedding → Vector Search → Top-K Docs   │
 * │                                                             │
 * │ Stage 2: AUGMENTATION                                       │
 * │   System Prompt + Retrieved Context + User Question          │
 * │   → Combined Prompt                                         │
 * │                                                             │
 * │ Stage 3: GENERATION                                         │
 * │   Combined Prompt → AI Model (Claude) → Grounded Answer     │
 * └─────────────────────────────────────────────────────────────┘
 *
 * BENEFITS OF RAG:
 * 1. ACCURACY: Answers are grounded in your actual data
 * 2. FRESHNESS: No need to retrain the model — just update the vector store
 * 3. TRANSPARENCY: You can show which documents were used to generate the answer
 * 4. COST: Cheaper than fine-tuning the model on your data
 * 5. PRIVACY: Your data stays in your vector store, not in the model's weights
 *
 * RAG vs. FINE-TUNING:
 * - RAG: Best for factual Q&A over dynamic data (commission rates, deal info)
 * - Fine-tuning: Best for teaching the model new skills or behavior patterns
 * - In practice, RAG is the preferred approach for enterprise applications
 *
 * COMPARISON TO TRADITIONAL SEARCH:
 * Traditional: User searches → keyword match → show results
 * RAG: User asks question → semantic search → AI synthesizes answer
 *
 * Example:
 * Traditional: "commission rate" → Returns all docs mentioning "commission rate"
 * RAG: "What's the best commission plan for a $50K deal?" → AI analyzes plans,
 *       finds the relevant tier, and explains which plan maximizes earnings
 */
@Service
public class CommissionRagService {

    private static final Logger log = LoggerFactory.getLogger(CommissionRagService.class);

    private final ChatClient chatClient;
    private final EmbeddingSearchService searchService;

    public CommissionRagService(ChatClient commissionChatClient,
                                 EmbeddingSearchService searchService) {
        this.chatClient = commissionChatClient;
        this.searchService = searchService;
    }

    /**
     * Answers a natural language question about commissions using RAG.
     *
     * RAG PIPELINE IMPLEMENTATION:
     *
     * Step 1 — RETRIEVAL:
     *   The user's question is converted to a vector and used to search
     *   the vector store for the most relevant documents. We retrieve
     *   the top 5 most similar documents.
     *
     * Step 2 — AUGMENTATION:
     *   The retrieved documents are formatted as context and injected
     *   into the prompt alongside the user's original question.
     *   The system prompt instructs the AI to use this context.
     *
     * Step 3 — GENERATION:
     *   The augmented prompt is sent to Claude, which generates a
     *   response grounded in the retrieved context.
     *
     * @param question A natural language question about commissions
     * @return An AI-generated answer grounded in commission data
     */
    @Observed(name = "commission.rag.answer", contextualName = "rag-answer-question")
    public String answerQuestion(String question) {
        log.info("RAG query: '{}'", question);

        // STAGE 1: RETRIEVAL — Find relevant documents
        List<Document> relevantDocs = searchService.search(question, 5);
        String context = searchService.extractContext(relevantDocs);

        log.info("Retrieved {} relevant documents for RAG context", relevantDocs.size());

        // STAGE 2 & 3: AUGMENTATION + GENERATION
        // The ChatClient sends the augmented prompt to Claude
        String response = chatClient.prompt()
                .system("""
                        You are a commission calculation expert assistant.
                        Use ONLY the provided context to answer the question.
                        If the context doesn't contain enough information, say so clearly.
                        Always cite which pieces of context you used in your answer.
                        Be precise with numbers and format currency as $X,XXX.XX.
                        """)
                .user(String.format("""
                        Context from our commission database:
                        %s

                        Question: %s

                        Please answer based on the context above.
                        """, context, question))
                .call()
                .content();

        log.info("RAG response generated successfully");
        return response;
    }

    /**
     * Answers a question about a specific type of commission data.
     *
     * FILTERED RAG:
     * This variant demonstrates using metadata filters in the retrieval
     * stage to narrow the search to a specific document type.
     *
     * Use cases:
     * - "Tell me about deal X" → filter type = "deal"
     * - "What commission plans are available?" → filter type = "commission_plan"
     * - "Who are the top performers?" → filter type = "user"
     *
     * @param question     The user's question
     * @param documentType The type of document to search (deal, commission_plan, etc.)
     * @return An AI-generated answer filtered to the specified domain
     */
    public String answerTypedQuestion(String question, String documentType) {
        log.info("Filtered RAG query: '{}' (type={})", question, documentType);

        // STAGE 1: FILTERED RETRIEVAL
        List<Document> relevantDocs = searchService.searchByType(question, documentType, 5);
        String context = searchService.extractContext(relevantDocs);

        // STAGE 2 & 3: AUGMENTATION + GENERATION
        String response = chatClient.prompt()
                .system(String.format("""
                        You are a commission calculation expert assistant.
                        You are answering questions specifically about %s data.
                        Use ONLY the provided context to answer the question.
                        If the context doesn't contain enough information, say so clearly.
                        """, documentType.replace("_", " ")))
                .user(String.format("""
                        Context (%s data):
                        %s

                        Question: %s
                        """, documentType, context, question))
                .call()
                .content();

        return response;
    }

    /**
     * Generates a comprehensive commission analysis report using RAG.
     *
     * MULTI-RETRIEVAL RAG:
     * This method demonstrates a more sophisticated RAG pattern where
     * we make MULTIPLE retrieval queries to gather context from different
     * document types, then combine them into a single rich context.
     *
     * This is useful for complex queries that span multiple domain concepts
     * (e.g., "Analyze this rep's performance" needs deals + calculations + plans).
     *
     * @param salesRepName The name of the sales representative to analyze
     * @return A comprehensive AI-generated analysis report
     */
    @Observed(name = "commission.rag.report", contextualName = "rag-performance-report")
    public String generatePerformanceReport(String salesRepName) {
        log.info("Generating performance report for: {}", salesRepName);

        // Multi-retrieval: search across different document types
        String repQuery = "sales representative " + salesRepName;

        List<Document> userDocs = searchService.searchByType(repQuery, "user", 2);
        List<Document> dealDocs = searchService.searchByType(repQuery + " deals", "deal", 5);
        List<Document> calcDocs = searchService.searchByType(
                repQuery + " commission calculations", "commission_calculation", 5);
        List<Document> planDocs = searchService.searchByType(
                "commission plan rates tiers", "commission_plan", 3);

        // Combine contexts from multiple retrievals
        String combinedContext = String.format("""
                === Sales Representative Info ===
                %s

                === Deals ===
                %s

                === Commission Calculations ===
                %s

                === Commission Plans ===
                %s
                """,
                searchService.extractContext(userDocs),
                searchService.extractContext(dealDocs),
                searchService.extractContext(calcDocs),
                searchService.extractContext(planDocs)
        );

        // Generate comprehensive report
        String response = chatClient.prompt()
                .system("""
                        You are a senior sales analytics consultant.
                        Generate a comprehensive performance report based on the provided data.
                        Include: summary, deal analysis, commission analysis, and recommendations.
                        Format the report with clear sections and bullet points.
                        """)
                .user(String.format("""
                        Generate a performance report for sales representative: %s

                        Available Data:
                        %s

                        Please provide a comprehensive analysis.
                        """, salesRepName, combinedContext))
                .call()
                .content();

        return response;
    }
}
