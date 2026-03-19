# Spring AI Module — Commission Calculator AI Integration

## Overview

This module demonstrates **Spring AI** framework concepts by integrating **Anthropic Claude AI** (Sonnet 4.5) into the Commission Calculator application. It builds on top of the existing ORM module's entities, repositories, and services to add AI-powered analysis, semantic search, and intelligent Q&A capabilities.

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     REST API Layer                                │
│  AiCommissionController (/api/ai/**)                             │
├──────────────────────────────────────────────────────────────────┤
│                     AI Service Layer                              │
│  CommissionExplainerService  │  DisputeAnalysisService           │
│  ForecastingService          │  AnomalyDetectionService          │
├──────────────────────────────────────────────────────────────────┤
│                     RAG Pipeline                                  │
│  CommissionRagService (Retrieval → Augmentation → Generation)    │
├──────────────────────────────────────────────────────────────────┤
│                     Prompt Engineering                            │
│  PromptTemplateService  │  .st template files                    │
├──────────────────────────────────────────────────────────────────┤
│                     Vector Store & Embeddings                     │
│  CommissionDocumentService  │  EmbeddingSearchService            │
│  SimpleVectorStore          │  Transformers EmbeddingModel       │
├──────────────────────────────────────────────────────────────────┤
│                     ORM Layer (Reused)                            │
│  Entities  │  Repositories  │  Services                          │
├──────────────────────────────────────────────────────────────────┤
│                     H2 Database                                   │
└──────────────────────────────────────────────────────────────────┘
```

## Concepts Covered

### 1. Spring AI Framework Setup and Configuration

**Files:** `config/AiConfig.java`, `config/VectorStoreConfig.java`, `application.properties`

Spring AI provides a Spring-native abstraction layer over AI model providers. Key configuration concepts:

- **Auto-Configuration**: Adding `spring-ai-anthropic-spring-boot-starter` automatically registers `AnthropicChatModel` and `ChatClient.Builder` beans
- **ChatModel vs ChatClient**: `ChatModel` is the low-level API (like `DataSource`), while `ChatClient` is the fluent high-level API (like `JdbcTemplate`)
- **Properties-Based Configuration**: Model selection, API keys, temperature, and max tokens are configured via `application.properties`
- **Bean Customization**: `AiConfig.java` creates a custom `ChatClient` with a domain-specific system prompt

**Key Properties:**
```properties
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-sonnet-4-5-20250514
spring.ai.anthropic.chat.options.max-tokens=1024
spring.ai.anthropic.chat.options.temperature=0.3
```

### 2. AI Model Integration with Claude

**Files:** `ml/CommissionExplainerService.java`, `ml/DisputeAnalysisService.java`, `ml/ForecastingService.java`, `ml/AnomalyDetectionService.java`

These services demonstrate integrating Claude AI into business workflows:

- **CommissionExplainerService**: Uses Claude to generate natural language explanations of commission calculations (Explainable AI / XAI)
- **DisputeAnalysisService**: AI-powered dispute analysis with both full analysis (template-driven) and quick triage (inline prompt)
- **ForecastingService**: Combines historical data with AI analysis for commission forecasting
- **AnomalyDetectionService**: Hybrid approach — Java computes statistics (mean, std deviation), AI interprets the results

**Integration Pattern:**
```
JPA Repository → Load Domain Data → Build Prompt → ChatClient → Claude API → AI Response
```

### 3. Prompt Engineering and Template Management

**Files:** `prompt/PromptTemplateService.java`, `resources/prompts/*.st`

Prompt engineering techniques demonstrated:

| Technique | Description | Example |
|-----------|-------------|---------|
| **Role Assignment** | System prompt establishes AI persona | "You are an expert commission calculator assistant..." |
| **Context Injection** | Template variables provide domain data | `{dealTitle}`, `${dealValue}`, `{salesRepName}` |
| **Structured Output** | Numbered lists ensure complete responses | "Please provide: 1. ... 2. ... 3. ..." |
| **Chain-of-Thought** | Forces step-by-step reasoning | "Explain your reasoning step by step" |
| **Few-Shot Prompting** | Examples guide response format | Included in explainer prompts |

**Template Files:**
- `commission-analysis.st` — Commission calculation breakdown
- `dispute-analysis.st` — Dispute resolution analysis
- `commission-forecast.st` — Sales forecasting with historical data
- `anomaly-detection.st` — Statistical anomaly identification

**Template Syntax:**
```
Deal Information:
- Deal Title: {dealTitle}
- Deal Value: ${dealValue}
- Sales Representative: {salesRepName}
```

### 4. Vector Databases and Embedding Stores

**Files:** `config/VectorStoreConfig.java`, `vectorstore/CommissionDocumentService.java`, `vectorstore/EmbeddingSearchService.java`

Vector store concepts demonstrated:

- **Text → Embedding Pipeline**: Domain entities are converted to natural language descriptions, then embedded as 384-dimensional float vectors
- **SimpleVectorStore**: In-memory vector store compatible with H2 (no external DB needed)
- **Cosine Similarity Search**: Finds semantically similar documents by comparing vector angles
- **Metadata Filtering**: Post-search filtering by document type, status, or entity ID
- **Document Design**: Converting JPA entities into searchable `Document` objects with content + metadata

**How Semantic Search Works:**
```
"How much did the big enterprise deal earn?"
         ↓ (embed query)
   [0.12, -0.45, 0.78, ...]
         ↓ (cosine similarity)
   Find closest vectors
         ↓
   "Sales deal 'Acme Corp Enterprise License' worth $150,000..."
   (Similar meaning, different words!)
```

**Document Types:**
- `deal` — Sales deal information (title, value, status, dates)
- `commission_plan` — Plan details with tier structures
- `commission_calculation` — Calculation results and amounts
- `user` — Sales team member profiles

### 5. RAG (Retrieval-Augmented Generation) Implementation

**Files:** `rag/CommissionRagService.java`

RAG is the key technique for making AI answers accurate about YOUR data:

```
┌─────────────────────────────────────────────────────────┐
│ Stage 1: RETRIEVAL                                       │
│   User Question → Embed → Vector Search → Top-K Docs    │
│                                                         │
│ Stage 2: AUGMENTATION                                    │
│   System Prompt + Retrieved Context + User Question      │
│   → Combined Prompt                                     │
│                                                         │
│ Stage 3: GENERATION                                      │
│   Combined Prompt → Claude → Grounded Answer             │
└─────────────────────────────────────────────────────────┘
```

**Three RAG Patterns Demonstrated:**
1. **Basic RAG** (`answerQuestion`): General Q&A across all document types
2. **Filtered RAG** (`answerTypedQuestion`): Search restricted to one document type
3. **Multi-Retrieval RAG** (`generatePerformanceReport`): Multiple searches combined for comprehensive reports

**Why RAG vs. Fine-Tuning?**
- RAG: Best for factual Q&A over dynamic data (commission rates, deal info)
- Fine-tuning: Best for teaching new behaviors/skills
- RAG is preferred for enterprise applications because data stays current without retraining

## Package Structure

```
com.chapman.edu.commissions.ai/
├── CommissionCalculatorAiApplication.java    # Main application entry point
├── config/
│   ├── AiConfig.java                         # ChatClient configuration
│   ├── VectorStoreConfig.java                # Vector store setup
│   ├── SecurityConfig.java                   # Security (permits all for edu)
│   └── AiDataInitializer.java                # Sample data + vector store loading
├── prompt/
│   └── PromptTemplateService.java            # Prompt template management
├── vectorstore/
│   ├── CommissionDocumentService.java        # Entity → Document conversion
│   └── EmbeddingSearchService.java           # Semantic search service
├── rag/
│   └── CommissionRagService.java             # RAG pipeline implementation
├── ml/
│   ├── CommissionExplainerService.java       # AI-powered explanations
│   ├── DisputeAnalysisService.java           # AI dispute analysis
│   ├── ForecastingService.java               # AI commission forecasting
│   └── AnomalyDetectionService.java          # AI anomaly detection
├── service/
│   ├── agent/
│   │   ├── CommissionReActAgent.java         # ReAct reasoning agent
│   │   ├── CommissionToolRegistry.java       # Tool registration for agent
│   │   ├── AgentResult.java                  # Agent response object
│   │   ├── AgentStep.java                    # Single reasoning step
│   │   └── Tool.java                         # Tool definition
│   ├── moderation/
│   │   └── ModerationService.java            # Input validation & output sanitization
│   └── workflow/
│       ├── CommissionWorkflowOrchestrator.java  # Multi-agent orchestrator
│       ├── WorkflowAgent.java                # Agent interface
│       ├── WorkflowResult.java               # Workflow response object
│       └── WorkflowState.java               # Shared state between agents
├── controller/
│   ├── CommissionController.java             # REST API endpoints (/api/ai/**)
│   └── AiWebController.java                 # Thymeleaf UI endpoints (/ai/**)
└── README.md                                 # This file

resources/templates/ai/
├── layout.html          # Shared navigation and styles
├── dashboard.html       # AI features overview
├── rag.html             # RAG Q&A interface
├── explainer.html       # Commission explainer
├── disputes.html        # Dispute analysis
├── forecast.html        # Commission forecasting
├── anomaly.html         # Anomaly detection
├── moderation.html      # Moderation & guardrails
├── agent.html           # ReAct agent with reasoning chain
└── workflow.html        # Multi-agent workflow
```

## API Endpoints

### REST API (`/api/ai/**`)

#### RAG Endpoints

| Method | Endpoint                   | Description                              |
|--------|----------------------------|------------------------------------------|
| POST   | `/api/ai/rag/ask`          | Ask a question about commission data     |
| POST   | `/api/ai/rag/ask/{type}`   | Ask a filtered question by document type |
| GET    | `/api/ai/rag/report/{name}`| Generate a sales rep performance report  |

#### Explanation Endpoints

| Method | Endpoint                            | Description                     |
|--------|-------------------------------------|---------------------------------|
| GET    | `/api/ai/explain/calculation/{id}`  | Explain a commission calculation|
| GET    | `/api/ai/explain/plan/{id}`         | Explain a commission plan       |

#### Dispute Analysis Endpoints

| Method | Endpoint                         | Description            |
|--------|----------------------------------|------------------------|
| GET    | `/api/ai/disputes/analyze/{id}`  | Full dispute analysis  |
| GET    | `/api/ai/disputes/triage/{id}`   | Quick priority triage  |

#### Forecasting Endpoints

| Method | Endpoint                     | Description           |
|--------|------------------------------|-----------------------|
| GET    | `/api/ai/forecast/user/{id}` | Individual forecast   |
| GET    | `/api/ai/forecast/team`      | Team-level forecast   |

#### Anomaly Detection Endpoints

| Method | Endpoint                       | Description                          |
|--------|--------------------------------|--------------------------------------|
| GET    | `/api/ai/anomaly/detect`       | Detect anomalies in all calculations |
| GET    | `/api/ai/anomaly/check/{id}`   | Check a single calculation           |

#### Moderation & Guardrails Endpoints

| Method | Endpoint                       | Description                                  |
|--------|--------------------------------|----------------------------------------------|
| POST   | `/api/ai/moderation/validate`  | Validate input against guardrail checks      |
| POST   | `/api/ai/moderation/classify`  | AI-powered content classification            |
| POST   | `/api/ai/moderation/sanitize`  | Redact sensitive data from text              |

#### ReAct Agent Endpoints

| Method | Endpoint              | Description                                    |
|--------|-----------------------|------------------------------------------------|
| POST   | `/api/ai/agent/ask`   | Execute ReAct agent with reasoning chain       |
| GET    | `/api/ai/agent/tools` | List available tools for the agent             |

#### Agentic Workflow Endpoints

| Method | Endpoint                  | Description                                |
|--------|---------------------------|--------------------------------------------|
| POST   | `/api/ai/workflow/review` | Execute multi-agent commission review      |
| GET    | `/api/ai/workflow/agents` | List registered agents in the pipeline     |

### Web UI (`/ai/**`)

| Route             | Page               | Description                                         |
|-------------------|--------------------|-----------------------------------------------------|
| `/ai`             | Dashboard          | Overview of all AI features with navigation          |
| `/ai/rag`         | RAG Q&A            | Ask questions and generate performance reports       |
| `/ai/explainer`   | Explainer          | Explain calculations and plans in plain language     |
| `/ai/disputes`    | Dispute Analysis   | Full analysis and quick triage                       |
| `/ai/forecast`    | Forecasting        | Individual and team commission forecasts             |
| `/ai/anomaly`     | Anomaly Detection  | Detect anomalies across all or single calculations   |
| `/ai/moderation`  | Moderation         | Input validation, classification, output sanitization|
| `/ai/agent`       | ReAct Agent        | Ask complex questions with visible reasoning chain   |
| `/ai/workflow`    | Agentic Workflow   | Multi-agent orchestrated reviews with stage log      |

## Running the Application

### Prerequisites

1. **Java 21** — required by the project (`java.version` in `pom.xml`)
2. **Maven 3.9+** — for building and running
3. **Anthropic API Key** — required for AI features that call Claude (RAG, agent, workflow, classification, etc.)

### Step 1: Set Your API Key

The application needs an Anthropic API key to call Claude. Set it as an environment variable:

**Linux / macOS:**
```bash
export SPRING_AI_ANTHROPIC_API_KEY=your-actual-api-key
```

**Windows (PowerShell):**
```powershell
$env:SPRING_AI_ANTHROPIC_API_KEY = "your-actual-api-key"
```

**Windows (Command Prompt):**
```cmd
set SPRING_AI_ANTHROPIC_API_KEY=your-actual-api-key
```

Alternatively, set it directly in `src/main/resources/application.properties`:
```properties
spring.ai.anthropic.api-key=your-actual-api-key
```

> **Note:** Never commit API keys to version control. Use environment variables in production.

### Step 2: Build the Project

```bash
mvn clean compile
```

### Step 3: Run the Application

```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.ai.CommissionCalculatorAiApplication
```

The application starts on **port 8081** by default (configured in `application.properties`).

### Step 4: Access the Application

**Web UI (Thymeleaf):**
- Open your browser and go to: **http://localhost:8081/ai**
- The dashboard provides navigation to all AI features
- Each page has interactive forms that call the REST API

**REST API (curl/Postman):**
```bash
# Ask a RAG question
curl -X POST http://localhost:8081/api/ai/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What commission plans are available?"}'

# Explain a calculation
curl http://localhost:8081/api/ai/explain/calculation/1

# Detect anomalies
curl http://localhost:8081/api/ai/anomaly/detect

# Get team forecast
curl http://localhost:8081/api/ai/forecast/team

# Ask the ReAct agent
curl -X POST http://localhost:8081/api/ai/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How much commission did Alice earn on her enterprise deals?"}'

# Execute a workflow review
curl -X POST http://localhost:8081/api/ai/workflow/review \
  -H "Content-Type: application/json" \
  -d '{"request": "Review Alice Johnson commission performance"}'

# Validate input through moderation
curl -X POST http://localhost:8081/api/ai/moderation/validate \
  -H "Content-Type: application/json" \
  -d '{"input": "What are the commission rates?"}'

# Sanitize sensitive data
curl -X POST http://localhost:8081/api/ai/moderation/sanitize \
  -H "Content-Type: application/json" \
  -d '{"text": "Contact john@example.com, SSN 123-45-6789"}'
```

### Step 5: Run Tests

```bash
# Run all tests (does NOT require an API key — tests use mocks)
mvn test

# Run only AI module tests
mvn test -Dtest="com.chapman.edu.commissions.ai.**"
```

> **Note:** Tests use Mockito mocks for `ChatClient`/`ChatModel`, so no API key is needed to run them.

### Troubleshooting

| Issue | Solution |
|-------|----------|
| `spring.ai.anthropic.api-key` is empty | Set the `SPRING_AI_ANTHROPIC_API_KEY` environment variable |
| Port 8081 already in use | Change `server.port` in `application.properties` or stop the other process |
| Tests fail with context load errors | Ensure you have Java 21 installed (`java -version`) |
| AI endpoints return errors | Verify your API key is valid and has available credits |
| Vector store is empty | The `AiDataInitializer` loads sample data on startup automatically |

## Dependencies Added (pom.xml)

```xml
<!-- Anthropic Claude AI integration -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>

<!-- Local embedding model (ONNX Transformers) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-transformers-spring-boot-starter</artifactId>
</dependency>

<!-- In-memory vector store (H2 compatible) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-simple</artifactId>
</dependency>
```

## Key Takeaways for Students

1. **Spring AI follows Spring patterns** — If you know Spring Boot, you already know how to configure AI. Properties, auto-configuration, dependency injection, and service layers all work the same way.

2. **AI is a layer, not a replacement** — The AI module sits ON TOP of the existing ORM layer. It enhances data with intelligence but doesn't replace structured queries, transactions, or business logic.

3. **Prompt engineering is software engineering** — Prompts are code. They should be versioned, tested, templated, and iterated on just like any other source code.

4. **RAG is the most practical AI pattern for enterprise apps** — It lets AI answer questions about your specific data without fine-tuning, keeps data fresh, and provides transparency about what information was used.

5. **Vector stores enable semantic understanding** — Moving from keyword search to meaning-based search is a paradigm shift. Users can ask questions in natural language and get relevant results even when their words don't exactly match the stored data.
