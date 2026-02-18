package com.chapman.edu.commissions.ai.processor;

import com.chapman.edu.commissions.ai.service.vectorstore.CommissionDocumentService;
import com.chapman.edu.commissions.ai.service.vectorstore.EmbeddingSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * PROCESSOR: SearchProcessor
 * ============================================================
 *
 * CONCEPT: Vector Databases and Embedding Stores for Semantic Search
 * -------------------------------------------------------------------
 * This processor demonstrates how vector databases enable semantic
 * (meaning-based) search in the Commission Calculator application.
 *
 * TRADITIONAL SEARCH vs. SEMANTIC SEARCH:
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │        TRADITIONAL (SQL LIKE / Full-Text)                       │
 * │                                                                 │
 * │  Query: "big enterprise deal earnings"                          │
 * │  SQL:   WHERE title LIKE '%big%' OR title LIKE '%enterprise%'   │
 * │  Result: Only finds docs containing exact words "big" or        │
 * │          "enterprise". Misses "Large Corporate Account".        │
 * ├─────────────────────────────────────────────────────────────────┤
 * │        SEMANTIC (Vector Embedding)                               │
 * │                                                                 │
 * │  Query: "big enterprise deal earnings"                          │
 * │  Process: Text → Vector [0.12, -0.45, ...] (384 dimensions)    │
 * │  Search: Find nearest vectors by cosine similarity              │
 * │  Result: Finds "Large Corporate Account — Revenue: $150K"       │
 * │          because "big enterprise" ≈ "large corporate"           │
 * │          and "earnings" ≈ "revenue" in vector space             │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * VECTOR STORE PIPELINE:
 *
 * 1. DOCUMENT INGESTION (CommissionDocumentService):
 *    Entity (Deal, Plan, User) → Natural language text → Document
 *    Document = { content: "Deal 'Acme Corp' worth $150K...",
 *                 metadata: { type: "deal", entityId: "deal-001" } }
 *
 * 2. EMBEDDING (EmbeddingModel — runs locally via ONNX):
 *    Document content → float[384] vector
 *    "Deal 'Acme Corp' worth $150K" → [0.12, -0.45, 0.78, ...]
 *
 * 3. STORAGE (SimpleVectorStore — in-memory):
 *    Vector + content + metadata stored in ConcurrentHashMap
 *
 * 4. SEARCH (EmbeddingSearchService):
 *    Query text → query vector → cosine similarity → top-K results
 *
 * SIMILARITY METRICS:
 * - Cosine Similarity: cos(θ) = (A·B)/(||A||×||B||)
 *   → 1.0 = identical meaning, 0.0 = unrelated
 * - Used by SimpleVectorStore for brute-force similarity search
 * - Production vector DBs use ANN (Approximate Nearest Neighbor) indexes
 *
 * METADATA FILTERING:
 * After vector similarity narrows candidates by meaning, metadata filters
 * further refine results by structured criteria:
 *   "Find documents similar to 'enterprise deals' WHERE type = 'deal'"
 */
@Service
public class SearchProcessor {

    private static final Logger log = LoggerFactory.getLogger(SearchProcessor.class);

    private final VectorStore vectorStore;
    private final EmbeddingSearchService searchService;
    private final CommissionDocumentService documentService;

    public SearchProcessor(VectorStore vectorStore,
                           EmbeddingSearchService searchService,
                           CommissionDocumentService documentService) {
        this.vectorStore = vectorStore;
        this.searchService = searchService;
        this.documentService = documentService;
    }

    // ============================================================
    // 1. BASIC SEMANTIC SEARCH
    // ============================================================

    /**
     * Demonstrates basic semantic search across all commission documents.
     *
     * SEARCH FLOW:
     * 1. User query ("high value deals") is converted to a vector
     * 2. Vector is compared against ALL stored document vectors
     * 3. Documents are ranked by cosine similarity score
     * 4. Top-K most similar documents are returned
     *
     * SEARCHREQUEST PARAMETERS:
     * - query: The natural language search text
     * - topK: Maximum results to return (default: 4)
     * - similarityThreshold: Minimum cosine similarity (0.0–1.0)
     *   → 0.5 = moderate relevance (used here)
     *   → 0.7 = high relevance (stricter, fewer results)
     *   → 0.3 = low relevance (lenient, more results)
     *
     * @param query Natural language search query
     * @param topK  Number of top results to return
     * @return List of search results with content and metadata
     */
    public List<Map<String, Object>> semanticSearch(String query, int topK) {
        log.info("Executing semantic search: '{}' (topK={})", query, topK);

        List<Document> results = searchService.search(query, topK);

        log.info("Semantic search returned {} results for: '{}'", results.size(), query);
        return formatResults(results);
    }

    // ============================================================
    // 2. FILTERED SEARCH — Metadata-Based Narrowing
    // ============================================================

    /**
     * Demonstrates metadata-filtered semantic search.
     *
     * METADATA FILTERING:
     * Combines the power of semantic similarity with structured filtering.
     * The vector search finds documents SIMILAR to the query, then the
     * metadata filter removes results that don't match the criteria.
     *
     * This is conceptually similar to:
     *   SELECT * FROM documents
     *   WHERE semantic_similarity(embedding, query_embedding) > 0.5
     *     AND type = 'deal'
     *   ORDER BY similarity DESC
     *   LIMIT topK;
     *
     * DOCUMENT TYPES IN COMMISSION CALCULATOR:
     * - "deal": Sales deals (title, value, status, close date)
     * - "commission_plan": Plans with tier structures and rates
     * - "commission_calculation": Calculated commissions with amounts
     * - "user": Sales team member profiles
     *
     * @param query        Natural language search query
     * @param documentType Document type to filter on
     * @param topK         Number of results to return
     * @return Filtered search results
     */
    public List<Map<String, Object>> filteredSearch(String query, String documentType, int topK) {
        log.info("Executing filtered search: '{}' (type={}, topK={})", query, documentType, topK);

        List<Document> results = searchService.searchByType(query, documentType, topK);

        log.info("Filtered search returned {} '{}' results for: '{}'",
                results.size(), documentType, query);
        return formatResults(results);
    }

    // ============================================================
    // 3. ENTITY-SPECIFIC SEARCH
    // ============================================================

    /**
     * Demonstrates entity-specific search using entityId metadata.
     *
     * ENTITY SEARCH USE CASE:
     * When you know the exact entity you're interested in but want to
     * find semantically related information about it. For example:
     * - "What bonuses apply to deal-001?" → Search with entityId filter
     * - "Explain calculation calc-001" → Find that specific calculation
     *
     * SIMILARITY THRESHOLD:
     * Entity searches use a LOWER threshold (0.3 vs 0.5) because:
     * - The entityId filter already ensures relevance
     * - We want to catch loosely related information about the entity
     * - Fewer false positives since the filter is precise
     *
     * @param query    What you want to know about the entity
     * @param entityId The specific entity ID to filter on
     * @return Documents matching both semantic query and entity ID
     */
    public List<Map<String, Object>> entitySearch(String query, String entityId) {
        log.info("Executing entity search for '{}': '{}'", entityId, query);

        List<Document> results = searchService.searchByEntity(query, entityId, 5);

        log.info("Entity search returned {} results for entity: {}", results.size(), entityId);
        return formatResults(results);
    }

    // ============================================================
    // 4. SEARCH WITH CUSTOM PARAMETERS — Direct VectorStore Access
    // ============================================================

    /**
     * Demonstrates direct VectorStore access with custom SearchRequest.
     *
     * ADVANCED SEARCH CONFIGURATION:
     * While EmbeddingSearchService provides convenient methods, sometimes
     * you need fine-grained control over the search parameters. This method
     * builds a SearchRequest directly using the builder pattern.
     *
     * SearchRequest.builder() options:
     * - .query(String): The search text
     * - .topK(int): Max results (default: 4)
     * - .similarityThreshold(double): Min similarity score (0.0–1.0)
     * - .filterExpression(Expression): Metadata filter
     *
     * FILTER EXPRESSION DSL:
     * FilterExpressionBuilder provides a fluent API for building filters:
     * - .eq("field", value): Equals
     * - .ne("field", value): Not equals
     * - .gt("field", value): Greater than
     * - .in("field", List): In set
     * - .and(expr1, expr2): Logical AND
     * - .or(expr1, expr2): Logical OR
     *
     * @param query               The search query
     * @param topK                Max results to return
     * @param similarityThreshold Minimum similarity score
     * @param documentType        Optional type filter (null for no filter)
     * @return Customized search results
     */
    public List<Map<String, Object>> customSearch(String query, int topK,
                                                   double similarityThreshold,
                                                   String documentType) {
        log.info("Executing custom search: '{}' (topK={}, threshold={}, type={})",
                query, topK, similarityThreshold, documentType);

        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold);

        // Conditionally add metadata filter
        if (documentType != null && !documentType.isBlank()) {
            FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
            requestBuilder.filterExpression(filterBuilder.eq("type", documentType).build());
        }

        List<Document> results = vectorStore.similaritySearch(requestBuilder.build());

        log.info("Custom search returned {} results", results.size());
        return formatResults(results);
    }

    // ============================================================
    // 5. COMPARATIVE SEARCH — Demonstrating Semantic Understanding
    // ============================================================

    /**
     * Demonstrates how semantic search understands meaning by comparing
     * results from different phrasings of the same question.
     *
     * SEMANTIC EQUIVALENCE:
     * The embedding model maps semantically similar phrases to nearby
     * vectors, so these queries should return similar results:
     * - "large corporate deals" ≈ "big enterprise contracts"
     * - "top earners" ≈ "highest commission recipients"
     * - "commission rates" ≈ "payout percentages"
     *
     * This demonstrates the key advantage over keyword search:
     * Users don't need to know the exact terminology used in the data.
     *
     * @param query1 First phrasing of the question
     * @param query2 Second phrasing (semantically similar)
     * @param topK   Number of results per query
     * @return A comparison map showing results for both queries
     */
    public Map<String, Object> compareSearchResults(String query1, String query2, int topK) {
        log.info("Comparing search results: '{}' vs '{}'", query1, query2);

        List<Document> results1 = searchService.search(query1, topK);
        List<Document> results2 = searchService.search(query2, topK);

        // Calculate overlap between result sets
        Set<String> ids1 = results1.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());
        Set<String> ids2 = results2.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        Set<String> overlap = new HashSet<>(ids1);
        overlap.retainAll(ids2);

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("query1", query1);
        comparison.put("query1Results", formatResults(results1));
        comparison.put("query1Count", results1.size());
        comparison.put("query2", query2);
        comparison.put("query2Results", formatResults(results2));
        comparison.put("query2Count", results2.size());
        comparison.put("overlappingDocuments", overlap.size());
        comparison.put("semanticSimilarityDemonstrated",
                !overlap.isEmpty() ? "Yes — different words, same meaning found same documents"
                        : "Queries may be too different semantically");

        log.info("Comparison complete: {} overlapping results", overlap.size());
        return comparison;
    }

    // ============================================================
    // 6. VECTOR STORE MANAGEMENT
    // ============================================================

    /**
     * Reloads all commission data into the vector store.
     *
     * DOCUMENT LOADING PIPELINE:
     * 1. CommissionDocumentService reads entities from JPA repositories
     * 2. Each entity is converted to a natural language Document:
     *    Deal entity → "Sales deal titled 'Acme Corp' with value of $150,000..."
     * 3. Documents include metadata for filtering: { type: "deal", entityId: "..." }
     * 4. VectorStore.add() embeds documents (text → float[384]) and stores them
     *
     * WHEN TO REFRESH:
     * - After new data is added to the database
     * - After data corrections or updates
     * - On application startup (handled by SampleDataLoader)
     *
     * PRODUCTION NOTE:
     * In production, you'd implement incremental updates instead of
     * full refreshes. Track which entities have changed since the last
     * load and only re-embed those documents.
     */
    public void refreshVectorStore() {
        log.info("Refreshing vector store with latest commission data");
        documentService.loadAllDocuments();
        log.info("Vector store refresh completed");
    }

    /**
     * Extracts the text content from search results for use as RAG context.
     *
     * CONTEXT EXTRACTION:
     * This is the bridge between the RETRIEVAL stage and the AUGMENTATION
     * stage of the RAG pipeline. Search results (Documents) are converted
     * to a formatted string that can be injected into an AI prompt.
     *
     * @param query The search query to execute
     * @param topK  Number of results to retrieve
     * @return Formatted context string from the search results
     */
    public String extractSearchContext(String query, int topK) {
        log.info("Extracting search context for: '{}' (topK={})", query, topK);

        List<Document> results = searchService.search(query, topK);
        String context = searchService.extractContext(results);

        log.info("Extracted context from {} documents", results.size());
        return context;
    }

    // ============================================================
    // UTILITY: Result Formatting
    // ============================================================

    /**
     * Converts Document search results into structured maps for API responses.
     *
     * Each result includes:
     * - content: The document's text content
     * - metadata: The document's metadata (type, entityId, status, etc.)
     * - id: The document's unique identifier in the vector store
     */
    private List<Map<String, Object>> formatResults(List<Document> documents) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rank", i + 1);
            result.put("content", doc.getText());
            result.put("metadata", doc.getMetadata());
            result.put("documentId", doc.getId());
            results.add(result);
        }
        return results;
    }
}
