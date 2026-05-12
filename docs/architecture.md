# Architecture

Tech Talent Pulse is a Java 21 Spring Boot ETL and dashboard platform foundation that is planned to convert public technology-signal data into recruiter-friendly trend intelligence.

## Architectural Style

The project will use a lightweight hexagonal architecture. The goal is to keep domain and transformation logic independent from infrastructure details while avoiding unnecessary ceremony for a portfolio-scale project.

## Planned Components

- **Ingestion adapters**: Fetch public source data, beginning with Stack Overflow questions through the Stack Exchange API.
- **Application services**: Coordinate ingestion, validation, transformation, and persistence workflows.
- **Domain model**: Represent technologies, source observations, metrics, trend snapshots, and source metadata.
- **Persistence adapters**: Store normalized data and analytics-ready metrics in PostgreSQL.
- **Dashboard/API layer**: Expose governed, recruiter-friendly trend views.
- **Operational governance**: Use documentation, review practices, validation checks, and ADRs to keep the project maintainable.

## Maven Directory Conventions

The Phase 1 foundation follows Maven conventions:

- `src/main/java`
- `src/main/resources`
- `src/test/java`
- `src/test/resources`

Current application code includes the Spring Boot entry point, application configuration, Flyway migrations, a basic context load test, a raw Stack Overflow ingestion foundation, and the first analytics transformation slice.

## Phase 1 Runtime Foundation

- Spring Boot Actuator provides health support.
- Spring Data JPA is available for future persistence work.
- Flyway owns schema evolution.
- Hibernate is configured to validate schema state, not create or update it.
- PostgreSQL is the only configured database target.
- Testcontainers supports PostgreSQL repository integration tests.
- No controllers, dashboard UI, authentication, Kafka/event streaming, or GitHub ingestion are implemented yet.

## Phase 2 Ingestion Foundation

The first ingestion source is Stack Overflow question activity through the Stack Exchange API v2.3 `/questions` endpoint. The implementation follows the existing lightweight hexagonal boundaries:

- `ingestion/domain`: provider, run status, and signal type concepts.
- `ingestion/application`: orchestration service and external client port.
- `ingestion/infrastructure/client`: Stack Overflow `RestClient` adapter.
- `ingestion/infrastructure/persistence`: JPA entities and repositories backed by Flyway-managed PostgreSQL tables.
- `ingestion/infrastructure/scheduling`: scheduled ingestion trigger, disabled by default.

Raw source records are persisted before any transformation. The `raw_technology_signal` table intentionally stores only provider metadata, source tag, raw JSON payload, and capture time.

## Phase 3 Analytics Transformation Foundation

The first transformation slice converts raw Stack Overflow question signals into daily tag-level trend snapshots:

- `transformation/domain`: lightweight metric records used by the transformation workflow.
- `transformation/application`: orchestration for reading raw payloads, parsing JSON with Jackson, grouping by provider/tag/UTC date, and calculating simple averages.
- `transformation/infrastructure/persistence`: JPA entity and repository for the Flyway-managed `technology_trend_snapshot` table.

The analytics table is intentionally narrow and dashboard-ready. It stores signal count, average score, average answer count, and capture time for one provider/tag/snapshot date combination.

## Data Flow

Planned MVP data flow:

1. Fetch public Stack Overflow question data for selected technology tags.
2. Normalize source data into stable internal records.
3. Persist raw source payloads for later transformation.
4. Transform raw records into daily metric snapshots.
5. Present recruiter-friendly summaries that include context and limitations.

## Design Principles

- Prefer explainable metrics over opaque scores.
- Keep external API concerns isolated in adapters.
- Keep domain and transformation logic testable without live network calls.
- Treat rate limits, source terms, and data freshness as first-class design concerns.
- Document meaningful architecture decisions with ADRs.
