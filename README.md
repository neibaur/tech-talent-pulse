# Tech Talent Pulse

Tech Talent Pulse is a planned Java 21 Spring Boot ETL and dashboard portfolio project that will synthesize GitHub activity, developer ecosystem signals, and labor-market data into recruiter-friendly technology trend intelligence.

Current status: **Phase 1 Spring Boot and PostgreSQL foundation**. This repository now contains the initial Maven build, minimal Spring Boot application entry point, PostgreSQL configuration, Flyway baseline migration, local Docker Compose service definition, and governance documentation.

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

The first MVP will focus on GitHub API data for Java ecosystem signals. Later phases may add additional public developer and labor-market signals.

Initial technology focus areas:

- Spring Boot
- Quarkus
- Micronaut
- Hibernate
- Kafka
- Maven
- Gradle
- JUnit
- Testcontainers

Potential future data sources include public package metadata, documentation activity, job posting aggregates, and survey or labor-market datasets where licensing and terms allow responsible use.

## Phased Roadmap

### Phase 0: Repository Foundation

- Establish project identity, documentation, governance, and decision records.
- Define MVP scope and architecture direction.
- Set security, review, and AI-assisted development expectations.

### Phase 1: Maven and Spring Boot Skeleton

- Introduce a Java 21 Maven project.
- Add Spring Boot application structure without production ETL behavior.
- Add baseline formatting, testing, and validation commands.

### Phase 2: GitHub Signal Ingestion

- Integrate with the GitHub API using environment-based configuration.
- Collect repository and activity signals for the MVP technology set.
- Persist normalized source records in PostgreSQL.

### Phase 3: Metrics and Analytics Model

- Transform raw source data into analytics-ready trend metrics.
- Add repeatable jobs, validation rules, and test coverage.
- Document metric definitions and limitations.

### Phase 4: Dashboard and Recruiter Insights

- Present trend summaries, comparisons, and explainable signal views.
- Add dashboard evidence for portfolio review.
- Harden operational and documentation workflows.

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

## Validation

Phase 1 validation commands:

- `mvn clean verify`
- `mvn test`
- `docker compose config`

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
