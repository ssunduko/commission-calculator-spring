# Observability Stack

Full local observability for the AI module: distributed tracing (Jaeger),
log aggregation (Loki), metrics (Prometheus), and a unified UI (Grafana).

## What's wired

| Pillar  | Producer (in app)                              | Backend     | UI                         |
|---------|------------------------------------------------|-------------|----------------------------|
| Traces  | Micrometer Tracing → OTLP HTTP (port 4318)     | Jaeger      | http://localhost:16686     |
| Logs    | Logback + loki4j push (port 3100)              | Loki        | http://localhost:3000      |
| Metrics | Micrometer Prometheus registry (`/actuator/prometheus`) | Prometheus | http://localhost:9090 |
| All     | -                                              | -           | http://localhost:3000 (Grafana) |

`@Observed` on AI service methods produces named spans like
`commission.rag.answer`, `commission.anomaly.detect`, and
`commission.agent.execute`.

## Run it

```bash
# 1. Start the observability stack
cd deploy/observability
docker compose up -d

# 2. Run the app pointing at the stack (from repo root)
cd ../..
./mvnw spring-boot:run -Pai

# 3. Hit an endpoint to generate traces/metrics/logs
curl -X POST http://localhost:8081/api/ai/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What commission plans are available?"}'
```

Then open Grafana, pick the **Commission AI — Overview** dashboard, and
click any traceId in the logs panel to jump straight to the span in Jaeger.

## Production note

`management.tracing.sampling.probability=1.0` (sample everything) is fine
in dev. Drop it to `0.05`–`0.1` in production, and consider running a
real OTel Collector between the app and Jaeger so you can fan out to
other backends.
