package com.chapman.edu.commissions.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ============================================================
 * SPRING BOOT APPLICATION: Commission Calculator AI Module
 * ============================================================
 *
 * This module demonstrates Spring AI concepts integrated with
 * the Commission Calculator domain:
 *
 * 1. SPRING AI FRAMEWORK SETUP — Configuration of Spring AI with
 *    Anthropic Claude as the AI model provider.
 *
 * 2. AI MODEL INTEGRATION — Using ChatClient and ChatModel to
 *    communicate with Claude for commission analysis, dispute
 *    resolution, and forecasting.
 *
 * 3. PROMPT ENGINEERING — Template-based prompt management with
 *    role assignment, context injection, and chain-of-thought
 *    prompting techniques.
 *
 * 4. VECTOR DATABASES — SimpleVectorStore with embedding models
 *    for semantic search over commission domain data.
 *
 * 5. RAG IMPLEMENTATION — Retrieval-Augmented Generation pipeline
 *    that retrieves relevant commission data from the vector store
 *    and augments AI prompts for grounded, accurate responses.
 *
 * COMPONENT SCANNING:
 * scanBasePackages includes BOTH the AI package and the ORM package.
 * This is because the AI module DEPENDS on ORM entities, repositories,
 * and services. Spring needs to scan both packages to wire everything together.
 *
 * This is a key architectural pattern: the AI layer sits ON TOP of
 * the data access layer (ORM), adding intelligence to existing data.
 *
 * ARCHITECTURE LAYERS:
 * ┌──────────────────────────────────┐
 * │  REST Controller (ai.controller) │  ← HTTP endpoints
 * ├──────────────────────────────────┤
 * │  AI Services (ai.ml, ai.rag)    │  ← AI business logic
 * ├──────────────────────────────────┤
 * │  Prompt Templates (ai.prompt)   │  ← Prompt engineering
 * ├──────────────────────────────────┤
 * │  Vector Store (ai.vectorstore)  │  ← Semantic search
 * ├──────────────────────────────────┤
 * │  ORM Layer (orm.*)              │  ← Data access (reused)
 * ├──────────────────────────────────┤
 * │  H2 Database                    │  ← Data storage
 * └──────────────────────────────────┘
 */
@SpringBootApplication(
    scanBasePackages = {
        "com.chapman.edu.commissions.ai",
        "com.chapman.edu.commissions.orm"
    }
)
@EnableTransactionManagement
public class CommissionCalculatorAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommissionCalculatorAiApplication.class, args);
    }
}
