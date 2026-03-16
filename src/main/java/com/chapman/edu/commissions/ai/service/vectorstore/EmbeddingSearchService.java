package com.chapman.edu.commissions.ai.service.vectorstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================
 * SPRING AI SERVICE: EmbeddingSearchService
 * ============================================================
 *
 * CONCEPT: Semantic Search with Vector Embeddings
 * ------------------------------------------------------------
 * Traditional search (SQL LIKE, full-text search) matches on KEYWORDS.
 * Semantic search matches on MEANING.
 *
 * EXAMPLE — Traditional vs. Semantic Search:
 *
 * User query: "How much money did the big enterprise deal earn?"
 *
 * Traditional (keyword): Searches for "big", "enterprise", "deal", "earn"
 *   → Might miss a document about "Large Corporate Account - Revenue: $50K"
 *   → Because none of the exact keywords match
 *
 * Semantic (embedding): Converts query to vector, finds similar vectors
 *   → Finds "Large Corporate Account" because "big enterprise" ≈ "large corporate"
 *   → Finds "Revenue: $50K" because "earn" ≈ "revenue"
 *
 * HOW SIMILARITY SEARCH WORKS:
 *
 * 1. Query text → query vector (via EmbeddingModel)
 * 2. Compare query vector to every stored vector
 * 3. Rank by similarity metric (cosine similarity)
 * 4. Return top-K most similar documents
 *
 * COSINE SIMILARITY:
 *   cos(θ) = (A · B) / (||A|| × ||B||)
 *   - 1.0 = identical meaning
 *   - 0.0 = unrelated
 *   - -1.0 = opposite meaning (rare with embeddings)
 *
 * SIMILARITY THRESHOLD:
 * A minimum similarity score filters out irrelevant results.
 * Typical thresholds: 0.7 (strict) to 0.5 (lenient).
 * Too low → noisy results; Too high → missing relevant results.
 *
 * METADATA FILTERING:
 * After vector similarity narrows the search, metadata filters can
 * further refine results. For example:
 *   - Only return documents where type = "deal"
 *   - Only return documents where status = "WON"
 * This combines semantic understanding with structured filtering.
 */
@Service
public class EmbeddingSearchService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingSearchService.class);

    private final VectorStore vectorStore;

    public EmbeddingSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Performs a basic semantic search across all documents.
     *
     * SearchRequest parameters:
     * - query: The natural language search text
     * - topK: Maximum number of results to return (default: 4)
     * - similarityThreshold: Minimum cosine similarity score (0.0 to 1.0)
     *
     * @param query   Natural language search query
     * @param topK    Number of top results to return
     * @return List of semantically similar documents
     */
    public List<Document> search(String query, int topK) {
        log.info("Performing semantic search: '{}' (topK={})", query, topK);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.5)  // Only return results with >50% similarity
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        log.info("Found {} results for query: '{}'", results.size(), query);

        return results;
    }

    /**
     * Performs a filtered semantic search — only searches documents of a specific type.
     *
     * METADATA FILTERING:
     * FilterExpressionBuilder creates filter expressions that are applied
     * AFTER the vector similarity search. This is efficient because:
     * 1. Vector search narrows down candidates by meaning
     * 2. Metadata filter removes candidates that don't match criteria
     * This is equivalent to: "Find documents similar to my query WHERE type = 'deal'"
     * FILTER EXPRESSION SYNTAX (Spring AI DSL):
     *   - .eq("field", value): Equals
     *   - .ne("field", value): Not equals
     *   - .gt("field", value): Greater than
     *   - .gte("field", value): Greater than or equal
     *   - .lt("field", value): Less than
     *   - .in("field", values): In list
     *   - .and(expr1, expr2): Logical AND
     *   - .or(expr1, expr2): Logical OR
     *
     * @param query        Natural language search query
     * @param documentType The type of document to search (deal, commission_plan, etc.)
     * @param topK         Number of top results to return
     * @return Filtered list of semantically similar documents
     */
    public List<Document> searchByType(String query, String documentType, int topK) {
        log.info("Performing filtered search: '{}' (type={}, topK={})", query, documentType, topK);
        // Build a metadata filter expression
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.5)
                .filterExpression(builder.eq("type", documentType).build())
                .build();
        List<Document> results = vectorStore.similaritySearch(request);
        log.info("Found {} results of type '{}' for query: '{}'", results.size(), documentType, query);

        return results;
    }

    /**
     * Searches for documents related to a specific entity by ID.
     *
     * This demonstrates combining semantic search with exact metadata matching.
     * Useful when you want to find documents ABOUT a specific entity
     * using natural language queries.
     *
     * @param query    Natural language description of what you're looking for
     * @param entityId The specific entity ID to filter on
     * @param topK     Number of top results to return
     * @return Documents matching both semantic and entity criteria
     */
    public List<Document> searchByEntity(String query, String entityId, int topK) {
        log.info("Searching for entity {}: '{}'", entityId, query);

        FilterExpressionBuilder builder = new FilterExpressionBuilder();

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.3) // Lower threshold for entity-specific search
                .filterExpression(builder.eq("entityId", entityId).build())
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * Extracts the text content from search results for use as context.
     *
     * This helper method is used by the RAG pipeline to convert
     * search results into a context string that can be injected
     * into an AI prompt.
     *
     * @param documents List of documents from a search
     * @return Concatenated text content from all documents
     */
    public String extractContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "No relevant information found.";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("[Result %d] %s\n", i + 1, doc.getText()));
        }
        return context.toString();
    }
}
