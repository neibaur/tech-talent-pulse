# Architecture

Tech Talent Pulse is a Java 21 Spring Boot ETL and dashboard platform foundation that is planned to convert public technology-signal data into recruiter-friendly trend intelligence.

## Architectural Style

The project uses a lightweight hexagonal/layered architecture. The goal is to keep ingestion, transformation, analytics, API, and persistence concerns separated while avoiding unnecessary ceremony for a portfolio-scale project. This balances maintainability and delivery speed: application services own workflow and calculations, repositories isolate persistence, controllers remain DTO-focused, and the project avoids premature job-framework or platform complexity.

## Planned Components

- **Ingestion adapters**: Fetch public source data, beginning with Stack Overflow questions through the Stack Exchange API.
- **Application services**: Coordinate ingestion, validation, transformation, and persistence workflows.
- **Domain model**: Represent technologies, source observations, metrics, trend snapshots, trend movement, and source metadata.
- **Persistence adapters**: Store normalized data and analytics-ready metrics in PostgreSQL.
- **Dashboard/API layer**: Expose governed, recruiter-friendly trend views.
- **Operational governance**: Use documentation, review practices, validation checks, and ADRs to keep the project maintainable.

## Maven Directory Conventions

The Phase 1 foundation follows Maven conventions:

- `src/main/java`
- `src/main/resources`
- `src/test/java`
- `src/test/resources`

Current application code includes the Spring Boot entry point, application configuration, Flyway migrations, a basic context load test, a raw Stack Overflow ingestion foundation, the first analytics transformation slice, and read-only dashboard API endpoints.

## Phase 1 Runtime Foundation

- Spring Boot Actuator provides health support.
- Spring Data JPA is available for future persistence work.
- Flyway owns schema evolution.
- Hibernate is configured to validate schema state, not create or update it.
- PostgreSQL is the only configured database target.
- Testcontainers supports PostgreSQL repository integration tests.
- No dashboard UI, authentication, Kafka/event streaming, or GitHub ingestion are implemented yet.

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

## Phase 5 Dashboard API Layer

The dashboard API layer exposes read-only views over analytics snapshots:

- `dashboard/domain`: API-facing dashboard concepts such as trend snapshots, summaries, and top-tag totals.
- `dashboard/application`: query orchestration, DTO-independent mapping, and safe limit handling.
- `dashboard/api`: Spring MVC controllers and response DTOs.

The controller layer does not expose JPA entities directly. It delegates query behavior to application services and returns HTTP 200 with empty collections for empty dashboard data. Authentication is intentionally deferred until a later phase with a defined security model.

## Phase 6 Demo Readiness

Phase 6 keeps the architecture backend-only and local-demo focused:

- API documentation is static Markdown under `docs/` rather than a runtime OpenAPI dependency.
- Demo data is enabled only by the explicit `demo` Spring profile.
- Demo seeding writes sample raw Stack Overflow-like signals through repositories and then invokes the existing analytics transformation service.
- Flyway migrations remain production-safe and do not insert demo-only records.
- Dashboard controllers continue returning DTOs rather than persistence entities.

## Phase 7A Orchestration Foundation

Phase 7A introduces an internal orchestration application service that synchronously coordinates
existing Stack Overflow ingestion and analytics transformation services. The orchestration layer
returns structured results for future trigger surfaces, while manual admin REST endpoints remain
deferred to Phase 7B.

## Phase 7B Manual Orchestration Triggers

Phase 7B exposes the orchestration application service through opt-in local/demo REST endpoints.
The controller is guarded by `tech-talent-pulse.admin.orchestration.enabled=true` and remains
disabled by default. These endpoints are operational demo tools, not authenticated production admin
APIs.

## Phase 7C Operational History Readback

Phase 7C adds an application service for recent ingestion run history using the existing
`ingestion_run` table. The guarded admin orchestration API returns DTOs with run metadata, status,
timestamps, error message, and operational counts, without exposing raw payloads.

## Phase 7D Local Smoke Validation

Phase 7D adds a dependency-free curl smoke script under `scripts/` so reviewers can validate the
local operational flow after the app is already running. It exercises health, dashboard read APIs,
manual orchestration triggers, and recent run history without adding frontend, authentication,
deployment infrastructure, queues, or scheduler changes.

## Phase 8A Advanced Analytics Foundation

Phase 8A adds a read-only analytics slice over existing `technology_trend_snapshot` data:

- `analytics/domain`: recruiter-facing trend movement records.
- `analytics/application`: calculation of latest-versus-previous deltas, safe percent changes, ranks,
  and rank movement.
- `analytics/api`: DTO-based endpoints for trend deltas and rising technologies.

The analytics service compares the most recent snapshot date with the previous available snapshot
date. Percent change is reported only when previous data exists and the previous signal count is
non-zero. Rising technologies are sorted by signal growth, rank movement, current signal count, and
tag name for deterministic output.

## Phase 8B Tag Comparison Analytics

Phase 8B extends the read-only analytics slice with tag comparison responses designed for future
dashboard charts. The comparison service accepts two to five normalized tags, uses a single
repository query for the requested tag history, preserves requested tag order, and returns:

- per-tag latest metrics;
- tag-local delta metrics against the previous point for that tag;
- current and previous rank context from the relevant snapshot dates;
- bounded historical points for time-series rendering;
- explicit missing-tag entries rather than failing partial comparisons.

The endpoint remains backend/API-only. No frontend, provider, authentication, deployment, queueing,
or new persistence table is introduced.

## Phase 8C Analytics Demo Readiness

Phase 8C closes Phase 8 by improving demo validation and API handoff documentation. The local smoke
script now exercises dashboard reads, Phase 8 analytics reads, guarded orchestration triggers, and
run history without asserting exact dataset values. Empty analytics responses remain valid when no
transformed snapshots exist.

No runtime OpenAPI/Swagger dependency is present, so endpoint documentation remains static Markdown.
Phase 8 analytics continue to compare UTC-normalized `snapshotDate` values produced by the Phase 3
transformation path; no local-time calculations are introduced in the analytics layer.

## Data Flow

Planned MVP data flow:

1. Fetch public Stack Overflow question data for selected technology tags.
2. Normalize source data into stable internal records.
3. Persist raw source payloads for later transformation.
4. Transform raw records into daily metric snapshots.
5. Serve recruiter-friendly trend data through read-only REST endpoints.
6. Seed explicit local demo data for reviewer walkthroughs when the `demo` profile is active.
7. Coordinate ingestion and transformation through an internal orchestration service.
8. Optionally trigger orchestration through local/demo admin endpoints when explicitly enabled.
9. Read back recent ingestion run history through local/demo admin endpoints when explicitly enabled.
10. Validate the local operational demo flow with the smoke script.
11. Calculate latest-versus-previous trend deltas and rising technology insights.
12. Compare two to five requested tags with latest metrics, deltas, rank movement, and bounded
    history.
13. Validate dashboard, analytics, and guarded operational endpoints through the local smoke script.
14. Present dashboard views that include context and limitations in a later UI phase.

## Design Principles

- Prefer explainable metrics over opaque scores.
- Keep external API concerns isolated in adapters.
- Keep domain and transformation logic testable without live network calls.
- Keep analytics calculations deterministic and explainable before adding broader scoring models.
- Treat rate limits, source terms, and data freshness as first-class design concerns.
- Document meaningful architecture decisions with ADRs.
