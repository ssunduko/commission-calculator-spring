# Commission Calculator - AI Module

A Spring Boot application featuring Spring AI integration with Anthropic Claude for intelligent commission management, including RAG pipelines, anomaly detection, forecasting, dispute analysis, and multi-agent workflows.

## Prerequisites

- **Docker** and **Docker Compose** (v2+)
- **Anthropic API key** ([get one here](https://console.anthropic.com/))

For local development without Docker:
- **Java 21**
- **Maven 3.9+**

## Quick Start (Docker)

### 1. Set your API key

```bash
export SPRING_AI_ANTHROPIC_API_KEY=your-key-here
```

On Windows (PowerShell):
```powershell
$env:SPRING_AI_ANTHROPIC_API_KEY="your-key-here"
```

### 2. Build and run

```bash
docker compose up --build
```

To run in the background:
```bash
docker compose up --build -d
```

### 3. Access the application

| Resource     | URL                                  |
|------------- |--------------------------------------|
| Web UI       | http://localhost:8081/ai             |
| REST API     | http://localhost:8081/api/ai/        |
| Swagger UI   | http://localhost:8081/swagger-ui/    |
| H2 Console   | http://localhost:8081/h2-console     |

H2 Console credentials: `admin` / `admin123`, JDBC URL: `jdbc:h2:mem:commissiondb`

### 4. Stop the application

```bash
docker compose down
```

To also remove the vector store data volume:
```bash
docker compose down -v
```

## Local Development (without Docker)

```bash
export SPRING_AI_ANTHROPIC_API_KEY=your-key-here

mvn clean compile
mvn spring-boot:run -Dspring-boot.run.mainClass=com.chapman.edu.commissions.ai.CommissionCalculatorAiApplication
```

## Project Structure

```
src/main/java/com/chapman/edu/commissions/ai/
├── config/          # Spring AI and security configuration
├── controller/      # REST and Thymeleaf controllers
├── dto/             # Request/response objects
├── processor/       # Educational Spring AI concept demos
├── repository/      # Data access layer
├── service/
│   ├── agent/       # ReAct agent with tool registry
│   ├── ml/          # Forecasting, anomaly detection, dispute analysis
│   ├── moderation/  # Input/output guardrails
│   ├── prompt/      # Template-based prompt engineering
│   ├── rag/         # Retrieval-Augmented Generation pipeline
│   ├── vectorstore/ # Embedding and semantic search
│   └── workflow/    # Multi-agent orchestration
└── util/            # Sample data loaders
```

## Key Features

- **RAG Pipeline** - Retrieval-Augmented Generation using vector store for context-aware responses
- **Commission Explainer** - Natural language explanations of commission calculations
- **Anomaly Detection** - Statistical analysis with AI-powered interpretation
- **Forecasting** - Commission trend prediction using historical data
- **Dispute Analysis** - AI-assisted dispute resolution recommendations
- **ReAct Agent** - Reasoning and action loop for complex queries
- **Multi-Agent Workflows** - Orchestrated pipelines (data gathering, compliance, anomaly analysis, reporting)
- **Moderation** - Input validation and output sanitization guardrails

## Docker Details

The Docker setup uses a multi-stage build:

1. **Build stage** - `maven:3.9-eclipse-temurin-21` compiles the application and packages it as a JAR
2. **Runtime stage** - `eclipse-temurin:21-jre` runs the JAR with minimal image size

Data persistence:
- Vector store data is persisted in the `vectorstore-data` Docker volume at `/app/data`
- The H2 database is in-memory and resets on container restart

## Environment Variables

| Variable                        | Required | Description                    |
|---------------------------------|----------|--------------------------------|
| `SPRING_AI_ANTHROPIC_API_KEY`   | Yes      | Anthropic API key for Claude   |

## API Endpoints

### AI Services (`/api/ai/`)

- `POST /api/ai/explain` - Explain a commission calculation
- `POST /api/ai/forecast` - Forecast future commissions
- `POST /api/ai/anomaly` - Detect commission anomalies
- `POST /api/ai/dispute` - Analyze a commission dispute
- `POST /api/ai/rag` - Query using RAG pipeline
- `POST /api/ai/agent` - Execute a ReAct agent query
- `POST /api/ai/workflow` - Run a multi-agent workflow

Full API documentation available at `/swagger-ui/` when the application is running.
