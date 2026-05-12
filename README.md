# Tech Talent Pulse

Tech Talent Pulse is a planned Java 21 Spring Boot ETL and dashboard portfolio project that will synthesize public developer activity, ecosystem signals, and labor-market data into recruiter-friendly technology trend intelligence.

Current status: **Phase 8A advanced analytics foundation**. This repository now contains the Maven/Spring Boot foundation, Stack Overflow signal ingestion, analytics transformations, read-only dashboard APIs, recruiter-facing trend delta and rising technology APIs, local demo data support, guarded orchestration endpoints, operational run history readback, and a local smoke validation script for reviewers.

## Value Proposition

Recruiters and hiring teams often need practical signals about which technologies are gaining traction, where developer communities are active, and how those trends may relate to talent demand. Tech Talent Pulse is designed to turn public technology-signal data into governed, explainable insights that are easier to evaluate than raw repository metrics or job postings alone.

The project is intended to demonstrate a production-minded approach to:

- Java 21 and Spring Boot service design.
- Maven-based build and dependency management.
- ETL pipeline design for public data sources.
- PostgreSQL-backed analytics modeling.
- API integration and rate-limit-aware ingestion.
- Dashboard-ready metric design.
- Documentation, governance, and delivery discipline.

## Planned Architecture

Tech Talent Pulse uses a lightweight hexagonal/layered architecture so ingestion, transformation, analytics, persistence, and presentation concerns remain separated without adding unnecessary framework ceremony. This shape was chosen to balance maintainability and delivery speed: it keeps core calculations testable, avoids premature job-framework complexity, and lets each phase add a narrow capability without rewriting earlier layers.

Planned layers include:

- **Ingestion adapters** for public APIs and external datasets.
- **Application services** for orchestration, validation, and transformation workflows.
- **Domain model** for technology signals, source metadata, metrics, and trend snapshots.
- **Persistence adapters** backed by PostgreSQL.
- **Dashboard/API layer** for recruiter-friendly reporting and exploration.
- **Governance controls** for documentation, review, validation, and secret handling.

Maven directory conventions will be used when source code is introduced:

- `src/main/java` for production Java code.
- `src/main/resources` for application resources and configuration templates.
- `src/test/java` for automated tests.
- `src/test/resources` for test resources.

## Planned Data Sources

The first implemented public technology-signal source is the Stack Exchange API for Stack Overflow questions. Later phases may add GitHub activity, package metadata, documentation activity, job posting aggregates, and survey or labor-market datasets where licensing and terms allow responsible use.

Initial technology focus areas:

- Spring Boot
- Java
- PostgreSQL
- Docker
- Kubernetes

## Phased Roadmap

### Phase 0: Repository Foundation

- Establish project identity, documentation, governance, and decision records.
- Define MVP scope and architecture direction.
- Set security, review, and AI-assisted development expectations.

### Phase 1: Maven and Spring Boot Skeleton

- Introduce a Java 21 Maven project.
- Add Spring Boot application structure without production ETL behavior.
- Add baseline formatting, testing, and validation commands.

### Phase 2: Stack Overflow Raw Signal Ingestion

- Integrate with the Stack Exchange API v2.3 questions endpoint for Stack Overflow.
- Collect recent question payloads for the initial target tags.
- Persist raw source records in PostgreSQL without analytics transformations.

### Phase 3: Metrics and Analytics Model

- Transform raw source data into analytics-ready trend metrics.
- Add repeatable jobs, validation rules, and test coverage.
- Document metric definitions and limitations.

### Phase 4: Quality and Coverage Hardening

- Enforce JaCoCo coverage gates in Maven verification.
- Publish CI coverage and test artifacts.
- Document future secret usage before external credentials are introduced.

### Phase 5: Dashboard API Layer

- Present trend summaries, comparisons, and explainable signal views.
- Add read-only REST endpoints for dashboard-ready metrics.
- Keep the API local/portfolio-focused before authentication and frontend work.

### Phase 6: API Demo Documentation

- Document the existing dashboard API endpoints and response shapes.
- Add an explicit local `demo` profile that seeds sample Stack Overflow-like data through the normal transformation path.
- Provide a recruiter-ready local runbook for PostgreSQL, app startup, demo data, curl checks, coverage reports, and validation.

### Phase 7A: Orchestration Foundation

- Add a synchronous application-layer orchestration service for ingestion, transformation, and combined runs.
- Return structured operational results for future trigger surfaces.

### Phase 7B: Manual Orchestration Triggers

- Add opt-in local/demo REST triggers for ingestion, transformation, and combined orchestration.
- Keep admin orchestration endpoints disabled unless explicitly enabled through configuration.
- Treat manual triggers as local operational tools, not production-authenticated admin APIs.

### Phase 7C: Operational History Readback

- Add guarded local/demo readback for recent ingestion runs using existing `ingestion_run` data.
- Keep history responses DTO-based and free of raw source payloads.
- Apply safe default and maximum limits for operational history queries.

### Phase 7D: Local Smoke Validation

- Add a curl-based local smoke script for the demo operational flow.
- Document manual curl fallback commands and troubleshooting guidance.
- Summarize Phase 7 operational capabilities and known limitations.

### Phase 8A: Advanced Analytics Foundation

- Add read-only trend delta analytics comparing the latest snapshot date to the previous snapshot date.
- Add rising technology insight responses sorted by signal growth and rank movement.
- Keep analytics responses DTO-based, deterministic, and safe around missing or zero previous data.

## Key Engineering Outcomes

- Reproducible local development with Docker Compose PostgreSQL, Flyway migrations, and a documented demo profile.
- CI/CD governance through Maven verification, Spotless, JaCoCo, CodeQL, Gitleaks, and Docker Compose validation.
- Flyway/Testcontainers parity so schema-managed persistence behavior is exercised against PostgreSQL in tests.
- Guarded operational APIs for local/demo ingestion, transformation, pipeline execution, and run history readback.
- Coverage discipline with the Maven JaCoCo bundle line coverage gate kept above 80%.
- Operational troubleshooting maturity through structured orchestration results, run history, logs, runbooks, and smoke validation.

## Skills Demonstrated

This project is designed to showcase:

- Backend engineering with Java 21, Spring Boot, Maven, and PostgreSQL.
- ETL design, data modeling, and API integration.
- Clean architecture and pragmatic domain boundaries.
- Test strategy using JUnit and planned Testcontainers support.
- DevOps and governance practices for a portfolio-grade repository.
- Security-conscious configuration and secret handling.
- Clear technical communication for engineering and recruiting audiences.

## Phase 1 Setup

Phase 1 introduces the minimal runtime foundation only. It does not include GitHub ingestion, ETL jobs, dashboard UI, external API clients, entities, repositories, or domain tables.

Local runtime configuration is environment-variable based:

- `TECH_TALENT_PULSE_DATASOURCE_URL`
- `TECH_TALENT_PULSE_DATASOURCE_USERNAME`
- `TECH_TALENT_PULSE_DATASOURCE_PASSWORD`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

No secret values are committed to the repository.

The local Docker Compose file defines a PostgreSQL service and a named volume for local persistence. The `demo` profile and Docker Compose share local-only database defaults for reviewer walkthroughs; production-like runs should use explicit environment configuration.

## Phase 2 Ingestion Foundation

Phase 2 adds a lightweight hexagonal ingestion slice for Stack Overflow question signals:

- `StackOverflowRestClient` uses Spring `RestClient` against the Stack Exchange API v2.3 questions endpoint.
- `StackOverflowIngestionService` orchestrates configured tag ingestion and duplicate checks.
- `ingestion_run` records ingestion execution metadata.
- `raw_technology_signal` stores raw JSON question payloads only.
- The scheduled job is disabled by default and must be enabled through configuration.

Phase 2 intentionally stopped at raw capture before analytics, dashboard UI, GitHub ingestion, Kafka/event streaming, or authentication.

## Phase 3 Analytics Transformations

Phase 3 adds the first analytics-ready table and transformation service:

- `TechnologyTrendTransformationService` reads raw Stack Overflow question payloads.
- Stack Overflow `creation_date` values are normalized to UTC daily snapshot dates.
- Daily tag/provider snapshots include signal count, average score, and average answer count.
- `technology_trend_snapshot` stores one row per provider, tag, and snapshot date.

No dashboard UI, additional providers, Kafka/event streaming, or authentication stack has been added.

## Phase 5 Dashboard API

Phase 5 adds read-only REST endpoints over `technology_trend_snapshot` records. These endpoints expose dashboard-ready metrics without exposing JPA entities or database internals.

Available endpoints:

- `GET /api/trends`: recent trend snapshots, ordered by newest snapshot date first.
- `GET /api/trends/{tag}`: trend history for one tag, ordered by newest snapshot date first.
- `GET /api/trends/summary`: top tags by signal count and the most recent snapshot date.

Optional query parameters:

- `limit`: default `50` for snapshot endpoints and `5` for summary top tags; maximum `500`.

Example local requests:

```bash
curl http://localhost:8080/api/trends
curl "http://localhost:8080/api/trends?limit=25"
curl http://localhost:8080/api/trends/java
curl "http://localhost:8080/api/trends/summary?limit=5"
```

The API returns HTTP 200 with empty collections when no trend data exists. Authentication is not added yet because this phase is scoped to a local portfolio MVP; production exposure should add an explicit security model first.

Detailed endpoint documentation is available in [docs/api-reference.md](docs/api-reference.md).

## Phase 6 Local Demo Workflow

Phase 6 adds demo readiness without adding a frontend, deployment infrastructure, authentication, or new ingestion providers.

The demo data seeder is disabled by default. With the `demo` profile active, the app uses a local Docker PostgreSQL JDBC URL and local-only database defaults that match Docker Compose. Environment variables can override those defaults. The seeder runs only when the `demo` Spring profile is active and `tech-talent-pulse.demo-data.enabled=true` is supplied explicitly. It inserts clearly marked sample raw Stack Overflow-like signals, then calls the existing analytics transformation service so dashboard responses are produced through the same transformation path as normal data.

Runbook:

- Start PostgreSQL with Docker Compose.
- Run the Spring Boot app with the `demo` profile, with or without explicit demo seeding.
- Call the dashboard API endpoints with curl.
- Run Maven and Docker Compose validation.
- View JaCoCo reports.

See [docs/local-demo-runbook.md](docs/local-demo-runbook.md) for the complete workflow.

## Phase 7A Orchestration Foundation

Phase 7A adds an internal application service that coordinates existing Stack Overflow ingestion and analytics transformation workflows. It returns structured results with status, provider, timing, ingestion counts, duplicate counts, transformed snapshot counts, and a short message. Manual admin REST endpoints are intentionally deferred to Phase 7B.

## Phase 7B Manual Orchestration Triggers

Phase 7B exposes the orchestration service through opt-in local/demo REST endpoints. They are disabled by default with `tech-talent-pulse.admin.orchestration.enabled=false`.

Enable them for a local demo run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments="--tech-talent-pulse.admin.orchestration.enabled=true"
```

Available manual triggers:

```bash
curl -X POST http://localhost:8080/api/admin/orchestration/ingestion
curl -X POST http://localhost:8080/api/admin/orchestration/transformation
curl -X POST http://localhost:8080/api/admin/orchestration/pipeline
```

These endpoints are local operational tools. They do not add authentication, authorization, deployment infrastructure, or a production admin surface.

## Phase 7C Operational History Readback

Phase 7C adds a guarded readback endpoint for recent ingestion run history. It reuses the existing `ingestion_run` table and is enabled by the same local/demo property as the manual triggers.

```bash
curl "http://localhost:8080/api/admin/orchestration/runs?limit=10"
```

The response includes run id, provider, status, timestamps, error message, and ingestion counts. Raw payloads and external API responses are not exposed.

## Phase 7D Local Smoke Validation

Phase 7D adds a lightweight smoke script for reviewers who have PostgreSQL and the Spring Boot app already running with admin orchestration enabled:

```bash
bash scripts/smoke-local-demo.sh
```

The script checks health, dashboard read APIs, manual orchestration triggers, and recent run history. Windows users can run it from Git Bash. See [docs/local-demo-runbook.md](docs/local-demo-runbook.md) for startup commands, manual curl equivalents, and troubleshooting notes.

## Phase 8A Advanced Analytics

Phase 8A adds recruiter-facing analytics over existing `technology_trend_snapshot` data.

Available endpoints:

- `GET /api/analytics/trends/deltas`: compares the most recent snapshot date with the previous snapshot date.
- `GET /api/analytics/trends/rising`: returns technologies with positive signal growth or positive rank movement.

Example local requests:

```bash
curl "http://localhost:8080/api/analytics/trends/deltas?limit=10"
curl "http://localhost:8080/api/analytics/trends/rising?limit=5"
```

Deltas include current and previous signal counts, absolute delta, percent change when previous data is non-zero, current rank, previous rank, and rank movement. Missing previous data is represented with `previousSignalCount` of `0` and nullable percent/rank fields.

## Validation

Current validation commands:

- `mvn clean verify`
- `mvn test`
- `docker compose config`

GitHub Actions also validates pull requests and pushes to `main` with Maven verification, Docker Compose configuration checks, Gitleaks secret scanning, and CodeQL Java analysis. If GitHub default CodeQL setup is already enabled for this repository, disable default setup before relying on the committed CodeQL workflow to avoid duplicate analysis.

## Coverage

`mvn clean verify` generates JaCoCo coverage reports and enforces the current coverage gate.

Local report outputs:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`

The current Maven gate requires at least 80% line coverage at the project bundle level. Branch coverage is visible in the report but is not enforced yet; it is a near-term hardening target as the codebase grows.

## Future Secret Readiness

No real API secrets, GitHub repository secrets, or environment secrets are required for the current phase.

Future phases may need secrets for:

- External API keys.
- Production database credentials.
- Cloud deployment tokens.
- Dashboard or BI integration credentials.

When those phases arrive, secrets should be configured through GitHub repository or environment secrets and referenced by environment variable name only. Do not commit `.env`, `.env.*`, example secret values, generated credentials, screenshots, logs, or local shell profiles.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
