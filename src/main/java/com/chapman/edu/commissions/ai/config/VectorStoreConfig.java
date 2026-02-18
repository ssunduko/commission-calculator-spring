package com.chapman.edu.commissions.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================
 * SPRING AI CONFIGURATION: VectorStoreConfig
 * ============================================================
 *
 * CONCEPT: Vector Databases and Embedding Stores
 * ------------------------------------------------------------
 * A vector store (vector database) is a specialized data store that
 * indexes and retrieves data based on SEMANTIC SIMILARITY rather than
 * exact keyword matching.
 *
 * HOW IT WORKS (Conceptual Pipeline):
 *
 * 1. TEXT → EMBEDDING:
 *    An embedding model converts text into a high-dimensional numeric vector
 *    (e.g., a 384-dimensional float array). Semantically similar texts
 *    produce vectors that are "close" to each other in vector space.
 *
 *    Example:
 *    "commission rate is 10%" → [0.12, -0.45, 0.78, ..., 0.33]  (384 dims)
 *    "sales bonus percentage" → [0.11, -0.44, 0.79, ..., 0.31]  (close!)
 *    "weather forecast today" → [-0.82, 0.15, -0.03, ..., 0.91] (far away)
 *
 * 2. STORAGE:
 *    The vector + original text + metadata are stored in the vector store.
 *    Each entry is called a "Document" in Spring AI.
 *
 * 3. SIMILARITY SEARCH:
 *    When a user asks a question, it's also converted to a vector, and the
 *    store finds the K nearest vectors (K-Nearest Neighbors / KNN).
 *    Distance metrics: Cosine Similarity, Euclidean Distance, Dot Product.
 *
 * VECTOR STORE OPTIONS IN SPRING AI:
 * - SimpleVectorStore: In-memory, file-backed (used here for H2 compatibility)
 * - PgVectorStore: PostgreSQL with pgvector extension (production)
 * - PineconeVectorStore: Pinecone cloud vector database
 * - ChromaVectorStore: Chroma open-source vector database
 * - MilvusVectorStore: Milvus distributed vector database
 * - WeaviateVectorStore: Weaviate vector search engine
 *
 * WHY SimpleVectorStore?
 * For this educational project using H2, SimpleVectorStore is ideal:
 * - No external database dependency
 * - Works entirely in-memory
 * - Supports save/load to JSON file for persistence
 * - Perfect for development and learning
 *
 * EMBEDDING MODEL:
 * We use the Transformers (ONNX) embedding model which runs locally.
 * This avoids needing a separate API call for embeddings.
 * The auto-configured model generates 384-dimensional vectors.
 */
@Configuration
public class VectorStoreConfig {

    /**
     * Creates a SimpleVectorStore backed by the auto-configured EmbeddingModel.
     *
     * SimpleVectorStore stores all vectors in-memory using a ConcurrentHashMap.
     * It performs brute-force cosine similarity search (comparing the query
     * vector against every stored vector). This is fine for educational use
     * with small datasets but would be too slow for production with millions
     * of documents — that's where ANN (Approximate Nearest Neighbor) indexes
     * in databases like PgVector or Pinecone come in.
     *
     * LIFECYCLE:
     * - Documents are added via vectorStore.add(List<Document>)
     * - Search via vectorStore.similaritySearch(SearchRequest)
     * - Data is lost on restart (in-memory) unless explicitly saved to file
     *
     * @param embeddingModel Auto-configured by spring-ai-transformers-spring-boot-starter.
     *                       Converts text → float[] vectors using a local ONNX model.
     * @return A VectorStore implementation for storing and searching document embeddings
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
