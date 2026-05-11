# MVP Scope

The first MVP for Tech Talent Pulse will focus on GitHub API data for Java ecosystem technology signals.

## MVP Goal

Build a governed ETL flow that collects public GitHub signals for selected technologies, transforms them into analytics-ready metrics, and prepares them for recruiter-friendly dashboard views.

## Initial Technology Set

- Spring Boot
- Quarkus
- Micronaut
- Hibernate
- Kafka
- Maven
- Gradle
- JUnit
- Testcontainers

## Planned Signal Categories

The MVP may evaluate public GitHub signals such as:

- Repository activity.
- Stars, forks, and watcher trends where available.
- Issue and pull request activity.
- Release cadence.
- Contributor activity.
- Topic and search metadata.
- Source freshness and collection timestamp.

Exact metrics will be documented before implementation and should include limitations.

## In Scope

- Java 21 and Spring Boot application foundation in a later phase.
- Maven-based build and validation.
- GitHub API ingestion for the initial technology set.
- PostgreSQL persistence.
- Repeatable ETL workflow design.
- Recruiter-friendly dashboard metrics and explanations.
- Tests for transformation logic and persistence behavior.

## Out Of Scope For MVP

- Private data sources.
- Credential collection beyond environment-variable-based configuration.
- Paid labor-market datasets.
- Real-time streaming requirements.
- Predictive hiring recommendations.
- Multi-tenant product features.
- Application code during Phase 0.

## Success Criteria

The MVP should make it possible to answer questions such as:

- Which Java ecosystem technologies show strong public development activity?
- Which signals are increasing, decreasing, or stable over a defined period?
- What evidence supports each trend summary?
- What limitations should a recruiter understand before using the metric?
