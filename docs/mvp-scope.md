# MVP Scope

The first MVP for Tech Talent Pulse begins with Stack Overflow question data from the Stack Exchange API and may later add GitHub API data for broader Java ecosystem technology signals.

## MVP Goal

Build a governed ETL flow that collects public technology signals for selected technologies, stores raw source payloads safely, and transforms them into analytics-ready metrics for later recruiter-friendly dashboard views.

## Initial Technology Set

- Java
- Spring Boot
- PostgreSQL
- Docker
- Kubernetes

## Planned Signal Categories

The MVP may evaluate public Stack Overflow signals such as:

- Recent question volume by tag.
- Question creation timestamps.
- Stack Overflow tags associated with each question.
- Average question score and answer count by tag and day.
- Source freshness and collection timestamp.

Later phases may add GitHub signals such as:

- Repository activity.
- Stars, forks, and watcher trends where available.
- Issue and pull request activity.
- Release cadence.
- Source freshness and collection timestamp.

Exact metrics will be documented before implementation and should include limitations.

## In Scope

- Java 21 and Spring Boot application foundation.
- Maven-based build and validation.
- Stack Exchange API ingestion for Stack Overflow question payloads.
- Daily tag-level analytics snapshots for Stack Overflow questions.
- Read-only REST endpoints for dashboard-ready trend snapshots and summaries.
- Static API documentation and a local demo data workflow for reviewer walkthroughs.
- PostgreSQL persistence.
- Repeatable ETL workflow design.
- Raw source capture with duplicate avoidance where practical.
- Recruiter-friendly dashboard metrics and explanations.
- Tests for transformation logic and persistence behavior.

## Out Of Scope For MVP

- Private data sources.
- Credential collection beyond environment-variable-based configuration.
- Paid labor-market datasets.
- Real-time streaming requirements.
- Predictive hiring recommendations.
- Multi-tenant product features.
- Additional providers during Phase 6.
- Dashboard UI during Phase 6.
- GitHub API ingestion during Phase 6.
- Authentication, public deployment, and production infrastructure during Phase 6.

## Success Criteria

The MVP should make it possible to answer questions such as:

- Which Java ecosystem technologies show strong public development activity?
- Which signals are increasing, decreasing, or stable over a defined period?
- What evidence supports each trend summary?
- What limitations should a recruiter understand before using the metric?
