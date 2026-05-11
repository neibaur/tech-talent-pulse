# Architecture

Tech Talent Pulse is a Java 21 Spring Boot ETL and dashboard platform foundation that is planned to convert public technology-signal data into recruiter-friendly trend intelligence.

## Architectural Style

The project will use a lightweight hexagonal architecture. The goal is to keep domain and transformation logic independent from infrastructure details while avoiding unnecessary ceremony for a portfolio-scale project.

## Planned Components

- **Ingestion adapters**: Fetch public source data, beginning with the GitHub API.
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

Current application code is limited to the Spring Boot entry point, application configuration, a Flyway baseline migration, and a basic context load test.

## Phase 1 Runtime Foundation

- Spring Boot Actuator provides health support.
- Spring Data JPA is available for future persistence work.
- Flyway owns schema evolution.
- Hibernate is configured to validate schema state, not create or update it.
- PostgreSQL is the only configured database target.
- Testcontainers dependencies are present for future PostgreSQL integration tests.
- No domain tables, ingestion clients, ETL jobs, controllers, entities, repositories, or services are implemented yet.

## Data Flow

Planned MVP data flow:

1. Fetch public GitHub activity and repository metadata for selected Java ecosystem technologies.
2. Normalize source data into stable internal records.
3. Transform records into trend metrics suitable for comparison and dashboard presentation.
4. Persist source records, metric snapshots, and run metadata in PostgreSQL.
5. Present recruiter-friendly summaries that include context and limitations.

## Design Principles

- Prefer explainable metrics over opaque scores.
- Keep external API concerns isolated in adapters.
- Keep domain and transformation logic testable without live network calls.
- Treat rate limits, source terms, and data freshness as first-class design concerns.
- Document meaningful architecture decisions with ADRs.
