# Tech Talent Pulse

Tech Talent Pulse is a planned Java 21 Spring Boot ETL and dashboard portfolio project that will synthesize public developer activity, ecosystem signals, and labor-market data into recruiter-friendly technology trend intelligence.

Current status: **Phase 6 API demo documentation**. This repository now contains the Maven/Spring Boot foundation, the first raw ingestion path for Stack Overflow question signals, an analytics transformation layer for daily tag-level trend snapshots, read-only REST endpoints for dashboard-ready metrics, and a local demo workflow for reviewers.

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

Tech Talent Pulse will use a lightweight hexagonal architecture so ingestion, transformation, persistence, and presentation concerns remain separated without adding unnecessary framework ceremony.

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

The local Docker Compose file defines a PostgreSQL service and a named volume for local persistence. It intentionally references environment variable names only.

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

The demo data seeder is disabled by default. It runs only when the `demo` Spring profile is active and `tech-talent-pulse.demo-data.enabled=true` is loaded from `application-demo.yml`. The seeder inserts clearly marked sample raw Stack Overflow-like signals, then calls the existing analytics transformation service so dashboard responses are produced through the same transformation path as normal data.

Runbook:

- Start PostgreSQL with Docker Compose.
- Run the Spring Boot app with the `demo` profile.
- Call the dashboard API endpoints with curl.
- Run Maven and Docker Compose validation.
- View JaCoCo reports.

See [docs/local-demo-runbook.md](docs/local-demo-runbook.md) for the complete workflow.

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
