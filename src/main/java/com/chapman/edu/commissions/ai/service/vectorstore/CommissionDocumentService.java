package com.chapman.edu.commissions.ai.service.vectorstore;

import com.chapman.edu.commissions.orm.entity.*;
import com.chapman.edu.commissions.orm.repository.CommissionCalculationRepository;
import com.chapman.edu.commissions.orm.repository.CommissionPlanRepository;
import com.chapman.edu.commissions.orm.repository.DealRepository;
import com.chapman.edu.commissions.orm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * SPRING AI SERVICE: CommissionDocumentService
 * ============================================================
 *
 * CONCEPT: Document Loading for Vector Stores
 * ------------------------------------------------------------
 * Before a vector store can be searched, it must be populated with
 * "documents" — chunks of text that represent your domain knowledge.
 *
 * THE DOCUMENT LOADING PIPELINE:
 *
 * 1. EXTRACT: Read data from your source (database, files, APIs)
 *    → Here we query JPA repositories for commission domain entities
 *
 * 2. TRANSFORM: Convert domain objects into text documents
 *    → Each entity becomes a natural language description
 *    → Metadata is attached for filtering and attribution
 *
 * 3. CHUNK (if needed): Split large documents into smaller pieces
 *    → For long documents, chunking improves search relevance
 *    → Our entity descriptions are small enough to skip this step
 *
 * 4. EMBED: Convert text → vectors (handled by VectorStore.add())
 *    → The EmbeddingModel converts each document's text to a float[]
 *
 * 5. STORE: Save vectors + metadata to the vector store
 *    → VectorStore.add() does steps 4 and 5 automatically
 *
 * DOCUMENT STRUCTURE IN SPRING AI:
 * - content: The text content (what gets embedded and searched)
 * - metadata: Key-value pairs for filtering (type, id, status, etc.)
 *
 * WHY NATURAL LANGUAGE?
 * Embedding models understand natural language better than raw data.
 * Instead of: "Deal: ABC-123, value: 50000, status: WON"
 * Use: "Sales deal titled 'ABC-123' worth $50,000 that has been won."
 * The second form produces better embeddings for semantic search.
 */
@Service
@Transactional(readOnly = true)
public class CommissionDocumentService {

    private static final Logger log = LoggerFactory.getLogger(CommissionDocumentService.class);

    private final SimpleVectorStore vectorStore;
    private final DealRepository dealRepository;
    private final CommissionPlanRepository planRepository;
    private final CommissionCalculationRepository calculationRepository;
    private final UserRepository userRepository;
    private final String vectorStoreFilePath;

    public CommissionDocumentService(SimpleVectorStore vectorStore,
                                     DealRepository dealRepository,
                                     CommissionPlanRepository planRepository,
                                     CommissionCalculationRepository calculationRepository,
                                     UserRepository userRepository,
                                     @Value("${app.vectorstore.file-path:data/vectorstore.json}") String vectorStoreFilePath) {
        this.vectorStore = vectorStore;
        this.dealRepository = dealRepository;
        this.planRepository = planRepository;
        this.calculationRepository = calculationRepository;
        this.userRepository = userRepository;
        this.vectorStoreFilePath = vectorStoreFilePath;
    }

    /**
     * Checks whether the vector store was already loaded from a persisted file.
     *
     * PERSISTENCE FLOW:
     * On startup, VectorStoreConfig attempts to load the store from disk.
     * If successful, the store already contains embeddings from a previous run,
     * and we can skip the expensive re-embedding step.
     *
     * This avoids re-computing embeddings for data that hasn't changed,
     * which is especially important because embedding is the slowest part
     * of the document loading pipeline.
     *
     * @return true if a persisted vector store file exists on disk
     */
    public boolean isVectorStorePersistedOnDisk() {
        return new File(vectorStoreFilePath).exists();
    }

    /**
     * Loads all commission domain data into the vector store.
     *
     * This method is called during application startup (from DataInitializer)
     * to populate the vector store with searchable content.
     *
     * PERSISTENCE:
     * After loading documents, the vector store is saved to a JSON file
     * on disk. On subsequent startups, the persisted file is loaded
     * directly by VectorStoreConfig, skipping the expensive embedding step.
     *
     * PERFORMANCE NOTE:
     * In production, you would:
     * 1. Use batch processing for large datasets
     * 2. Implement incremental updates (only embed new/changed records)
     * 3. Use a persistent vector store (e.g., PgVector) instead of file-based
     */
    public void loadAllDocuments() {
        log.info("Loading commission domain data into vector store...");

        List<Document> documents = new ArrayList<>();

        // Load deals as documents
        documents.addAll(loadDealDocuments());

        // Load commission plans as documents
        documents.addAll(loadPlanDocuments());

        // Load commission calculations as documents
        documents.addAll(loadCalculationDocuments());

        // Load user profiles as documents
        documents.addAll(loadUserDocuments());

        if (!documents.isEmpty()) {
            // VectorStore.add() automatically:
            // 1. Calls EmbeddingModel.embed() on each document's content
            // 2. Stores the resulting vectors alongside the text and metadata
            vectorStore.add(documents);
            log.info("Successfully loaded {} documents into vector store", documents.size());

            // Persist the vector store to disk so subsequent startups skip re-embedding
            persistVectorStore();
        } else {
            log.info("No documents to load into vector store (database may be empty)");
        }
    }

    /**
     * Saves the current vector store contents to a JSON file on disk.
     *
     * PERSISTENCE MECHANISM:
     * SimpleVectorStore.save(File) serializes all documents and their
     * embedding vectors to a JSON file. On next startup, load(File)
     * restores them without calling the EmbeddingModel again.
     *
     * This is a key optimization: embedding computation (text → vector)
     * is the most expensive step. By persisting the results, we turn
     * an O(n * embedding_time) startup into an O(n * file_read_time) startup.
     */
    private void persistVectorStore() {
        File storeFile = new File(vectorStoreFilePath);
        storeFile.getParentFile().mkdirs();
        vectorStore.save(storeFile);
        log.info("Vector store persisted to: {}", storeFile.getAbsolutePath());
    }

    /**
     * Converts Deal entities into searchable documents.
     *
     * DOCUMENT DESIGN PRINCIPLES:
     * - Content should read like a natural language description
     * - Include all fields that users might search for
     * - Metadata enables post-search filtering (e.g., find only WON deals)
     *
     * METADATA TYPES:
     * - "type": Categorizes documents for filtering (deal, plan, calculation, user)
     * - "entityId": Links back to the source entity for data retrieval
     * - Domain-specific fields: status, value ranges, dates
     */
    private List<Document> loadDealDocuments() {
        List<Document> documents = new ArrayList<>();
        List<Deal> deals = dealRepository.findAll();

        for (Deal deal : deals) {
            // Create natural language content from the entity
            String content = String.format(
                    "Sales deal titled '%s' with a value of $%s. " +
                    "Current status is %s. Created on %s.",
                    deal.getTitle(),
                    deal.getValue().toPlainString(),
                    deal.getStatus(),
                    deal.getCreatedDate()
            );

            if (deal.getCloseDate() != null) {
                content += String.format(" Closed on %s.", deal.getCloseDate());
            }

            // Metadata enables filtering without re-searching
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "deal");
            metadata.put("entityId", deal.getId());
            metadata.put("status", deal.getStatus().name());
            metadata.put("value", deal.getValue().doubleValue());

            documents.add(new Document(content, metadata));
        }

        log.info("Prepared {} deal documents", documents.size());
        return documents;
    }

    /**
     * Converts CommissionPlan entities into searchable documents.
     *
     * Plans are particularly important for RAG because users often
     * ask questions about plan structures, rates, and eligibility.
     */
    private List<Document> loadPlanDocuments() {
        List<Document> documents = new ArrayList<>();
        List<CommissionPlan> plans = planRepository.findAll();

        for (CommissionPlan plan : plans) {
            StringBuilder content = new StringBuilder();
            content.append(String.format(
                    "Commission plan named '%s' with status %s. ",
                    plan.getName(), plan.getStatus()
            ));
            content.append(String.format("Currency: %s. ", plan.getCurrency()));

            if (plan.getEffectiveStartDate() != null) {
                content.append(String.format("Effective from %s", plan.getEffectiveStartDate()));
                if (plan.getEffectiveEndDate() != null) {
                    content.append(String.format(" to %s", plan.getEffectiveEndDate()));
                }
                content.append(". ");
            }

            // Load tiers for richer content
            planRepository.findByIdWithTiers(plan.getId()).ifPresent(planWithTiers -> {
                List<CommissionTier> tiers = planWithTiers.getTiers();
                if (!tiers.isEmpty()) {
                    content.append("Commission tiers: ");
                    for (CommissionTier tier : tiers) {
                        content.append(String.format(
                                "%s (range $%s to %s, rate %s%%); ",
                                tier.getName(),
                                tier.getLowerBound().toPlainString(),
                                tier.getUpperBound() != null ? "$" + tier.getUpperBound().toPlainString() : "unlimited",
                                tier.getRate().toPlainString()
                        ));
                    }
                }
            });

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "commission_plan");
            metadata.put("entityId", plan.getId());
            metadata.put("status", plan.getStatus().name());
            metadata.put("planName", plan.getName());

            documents.add(new Document(content.toString(), metadata));
        }

        log.info("Prepared {} plan documents", documents.size());
        return documents;
    }

    /**
     * Converts CommissionCalculation entities into searchable documents.
     */
    private List<Document> loadCalculationDocuments() {
        List<Document> documents = new ArrayList<>();
        List<CommissionCalculation> calculations = calculationRepository.findAll();

        for (CommissionCalculation calc : calculations) {
            String content = String.format(
                    "Commission calculation with base commission $%s, " +
                    "gross commission $%s, and net commission $%s. " +
                    "Status: %s. Calculated on %s.",
                    calc.getBaseCommission().toPlainString(),
                    calc.getGrossCommission().toPlainString(),
                    calc.getNetCommission().toPlainString(),
                    calc.getStatus(),
                    calc.getCalculationDate()
            );

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "commission_calculation");
            metadata.put("entityId", calc.getId());
            metadata.put("status", calc.getStatus().name());
            metadata.put("baseCommission", calc.getBaseCommission().doubleValue());

            documents.add(new Document(content, metadata));
        }

        log.info("Prepared {} calculation documents", documents.size());
        return documents;
    }

    /**
     * Converts User entities into searchable documents.
     */
    private List<Document> loadUserDocuments() {
        List<Document> documents = new ArrayList<>();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String content = String.format(
                    "Sales team member %s %s (username: %s, email: %s). " +
                    "Department: %s. Territory: %s. Roles: %s. Active: %s.",
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getDepartment() != null ? user.getDepartment() : "N/A",
                    user.getTerritory() != null ? user.getTerritory() : "N/A",
                    user.getRoles(),
                    user.isActive()
            );

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "user");
            metadata.put("entityId", user.getId());
            metadata.put("username", user.getUsername());
            metadata.put("active", user.isActive());

            documents.add(new Document(content, metadata));
        }

        log.info("Prepared {} user documents", documents.size());
        return documents;
    }
}
