# AI GitHub Codebase Intelligence System

An AI-powered system that provides deep insights and conversational intelligence for GitHub repositories.

## Overview
This system implements a Retrieval-Augmented Generation (RAG) pipeline to ingest, chunk, embed, and query codebases. It is built with a microservices architecture using Spring Boot (Backend), Next.js (Frontend), Kafka (Messaging), and PostgreSQL.

## Architecture
See [docs/architecture.md](docs/architecture.md) for detailed service diagrams and data flow.

## Local Development

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local backend development)
- Node.js 20+ (for local frontend development)

### Running with Docker Compose
```bash
docker compose up --build
```
The frontend will be available at `http://localhost:3000` and the API Gateway at `http://localhost:8080`.

### Environment Setup
Copy `.env.example` to `.env` and adjust credentials/URLs for your environment.

Key runtime toggles:
- `AUTH_ENABLED=true` enables JWT/RBAC enforcement in API Gateway.
- `VECTOR_PROVIDER=inmemory|pinecone` switches vector backend implementation.
- `PINECONE_INDEX_URL` and `PINECONE_API_KEY` are required for `VECTOR_PROVIDER=pinecone`.

## API Examples

### 1. Ingest a Repository
```bash
curl -X POST http://localhost:8080/api/repos/ingest \
     -H "Content-Type: application/json" \
     -d '{"repoUrl": "https://github.com/example/repo", "branch": "main", "privateRepo": false, "oauthToken": ""}'
```

### 2. Query the Codebase
```bash
curl -X POST http://localhost:8080/api/query \
     -H "Content-Type: application/json" \
     -d '{"repoId": "some-uuid", "question": "How does the auth logic work?", "topK": 5, "sessionId": "chat-1"}'
```

## Implementation Notes
- **Kafka** handles high-throughput processing of large repositories.
- **Microservices** allow scaling individual steps (e.g., more embedding workers).
- **Next.js 14** provides a modern, responsive UI.
- **Vector storage** is currently in-memory in `vector-store-service` for local MVP speed.
- **LLM responses** are currently synthetic summaries in `llm-response-service` and can be replaced by external providers.

## Production Baseline Included
- Input validation for ingest/query requests (`jakarta.validation` on shared contracts).
- Structured API error responses in gateway for validation, downstream, and internal failures.
- Graceful shutdown enabled on all backend services.
- Actuator health probes and metrics endpoint exposure on all backend services.
- Kafka producer reliability defaults (acks/retries/idempotence on key producer services).
- Idempotent vector upsert behavior by `chunkId` to avoid duplicate growth.
- Container restart policies and per-service health checks in Docker Compose.
- Externalized runtime credentials and DB settings through environment variables.
- Optional JWT resource-server security with RBAC roles (`INGEST`, `QUERY`, `ADMIN`) in API Gateway.
- Pluggable vector backend (`inmemory` and `pinecone`) behind stable internal APIs.
- Prometheus + Grafana observability stack included in Docker Compose.
- CI workflow for backend/frontend builds and Docker/Compose validation.

## Observability
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001`

Each backend service exposes metrics at `/actuator/prometheus`.

## Limitations & Next Steps
- Replace in-memory vector store with Pinecone/Weaviate for durable retrieval at scale.
- Integrate real embedding and LLM providers with key management and rate limiting.
- Add authn/authz (OAuth/JWT + RBAC) on gateway and UI.
- Add CI/CD pipeline (tests, image scanning, SBOM, signed artifacts, staged deploys).
- Add distributed tracing (OpenTelemetry), alerting dashboards, and SLO/SLA policies.
- Implement OAuth for private repositories.
- Add support for diverse file types (.pdf, .ipynb).
- Enhance chunking strategies for multi-file context.
