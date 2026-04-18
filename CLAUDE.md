# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
mvn clean package
```

**Run:**
```bash
mvn spring-boot:run
```

**Test all:**
```bash
mvn test
```

**Run a single test class:**
```bash
mvn test -Dtest=TourGuideApiTest
```

**Run a single test method:**
```bash
mvn test -Dtest=TourGuideApiTest#whenValidInput_thenReturns200
```

## Architecture

This is a Spring Boot 3.5.4 REST API that generates AI-powered travel itineraries using Google Gemini. Java 21 virtual threads are enabled. The application is secured with Keycloak (OAuth2/JWT) and uses PostgreSQL for persistence. Secrets are managed via HashiCorp Vault.

### Async Request Flow

The API uses a two-step pattern: the client immediately receives a `tripId`, then subscribes to an SSE stream to receive results when generation completes.

```
POST /api/itinerary           → returns { tripId: uuid } immediately
GET  /api/subscribe/{tripId}  → SSE stream, emits "itinerary-complete" event
GET  /api/itineraries         → returns all saved itineraries for the user
GET  /api/health              → health check endpoint
```

Internally:
1. `ItineraryGenerationController` extracts JWT claims from the `Principal`, generates a `tripId`, calls `ItineraryGenerationService.createItineraryAsync()` — runs in a background thread via `@Async`
2. `UserPromptBuilder` constructs system + user prompts from the request
3. `LLMService` calls Gemini (or Ollama) via Spring AI's `ChatClient`
4. `SseService` holds in-memory SSE connections (`ConcurrentHashMap<tripId, SseEmitter>`) and delivers the result
5. `UserItineraryPersistenceService` saves the request and generated plan to PostgreSQL

### Package Structure

```
com.smarttours.atlasguidebackend/
├── domain/
│   ├── exceptions/           — UncompleteItineraryException, ItineraryPersistenceException
│   ├── location/             — Location models
│   ├── repositories/         — Repository interfaces (domain contracts)
│   ├── service/              — ItineraryGenerationService, LLMService, SseService,
│   │                           UserItineraryPersistenceService, ItineraryRetrievalService, LocationService
│   └── user/
│       ├── input/            — ItineraryRequest DTO + enums (Pace, Budget, TravelerType, TourGuidePersona)
│       └── output/           — ItineraryPlan, DayPlan, Event, FicheDeVisite
├── exposition/
│   ├── config/               — SecurityConfig, KeycloakJwtAuthenticationConverter, OpenApiConfig
│   ├── ItineraryGenerationController.java
│   ├── ItineraryRetrievalController.java
│   ├── LocationWrapperController.java
│   └── HealthcheckController.java
├── infrastructure/
│   ├── configs/              — GeminiChatClientConfiguration, OllamaChatClientConfiguration
│   ├── entities/             — UserItineraryRequest, ItineraryPlanEntity, DayPlanEntity,
│   │                           EventEntity, VisitCardEntity, Owner
│   └── repository/
│       ├── domain/           — ConcreteItineraryRepository
│       ├── jpa/              — Spring Data JPA interfaces
│       └── utils/            — ItineraryPlanMapper, UserItineraryRequestMapper
├── exception/                — GlobalExceptionHandler
├── utils/                    — UserPromptBuilder, AppConfig (ObjectMapper + async executor)
└── SmartToursGuideApp.java   — Main entry point
```

### LLM Configuration

Spring AI profiles manage LLM backends:
- `gemini` — Google Genai Gemini 2.5 Flash Lite; credentials via Vault or `GEMINI_CREDENTIALS_URI` env var
- `ollama` (default in local/test) — local Ollama instance; used in tests via `@ActiveProfiles({"ollama","test"})`

Default active profiles in `application.yml`: `ollama, debug`. Override to `gemini` for production.

### Authentication

Spring Security is configured as an OAuth2 resource server validating JWT tokens issued by Keycloak.

- `SecurityConfig` — stateless JWT auth, CORS (all origins in dev, restricted in prod)
- `KeycloakJwtAuthenticationConverter` — extracts roles from `realm_access` and `resource_access` JWT claims
- `OpenApiConfig` — Swagger UI OAuth2 integration with Keycloak authorization/token endpoints
- JWT issuer URI is configured via `KEYCLOAK_ISSUER_URI` environment variable

### Database

PostgreSQL is used for persistence (JPA/Hibernate). Schema is auto-managed via `spring.jpa.hibernate.ddl-auto=update`.

Key entities:
- `UserItineraryRequest` — stores raw user request JSON, generated response JSON, `tripId`, owner, timestamp
- `ItineraryPlanEntity` / `DayPlanEntity` / `EventEntity` / `VisitCardEntity` — structured itinerary storage with OneToMany relationships

Repositories follow a domain interface → `ConcreteItineraryRepository` → JPA repository pattern.

### Secrets Management (HashiCorp Vault)

Spring Cloud Vault is used to import secrets at startup:

| Profile        | Auth method              | Vault paths                               |
|----------------|--------------------------|-------------------------------------------|
| `vault-local`  | AppRole (role-id/secret) | `kv-tours/backend/{environment}/ai-config`, `/database` |
| `vault-oci`    | OCI Instance Principal   | Same paths                                |

Environment variables required: `VAULT_URI`, `VAULT_ROLE_ID`, `VAULT_SECRET_ID` (AppRole) or instance identity (OCI).

### Persona System

`TourGuidePersona` enum defines four travel guide personalities, each with embedded LLM instruction strings:
- `FRIENDLY_LOCAL_EXPERT` (default)
- `KNOWLEDGEABLE_HISTORIAN`
- `EFFICIENT_CONCIERGE`
- `ADVENTUROUS_STORYTELLER`

### Async Execution

`AppConfig` defines a `ThreadPoolTaskExecutor` bean (`smartToursTaskExecutor`) with core=5, max=10, queue=25. `ItineraryGenerationService` is annotated `@Async("smartToursTaskExecutor")`.

### Infrastructure as Code (iac/)

Terraform configuration for OCI (Oracle Cloud) deployment:

- `oci-vm.tf` — provisions an ARM compute instance (VM.Standard.A1.Flex, 2 OCPUs, 8GB RAM) with VCN, subnet, route table, security groups; bootstrap script installs Podman and runs Docker Compose for Vault, Keycloak, and the Java app
- `variables.tf` — OCI-specific variables (tenancy, compartment, region, etc.)
- `terraform.tfvars` — variable values (not committed to VCS)