# ADR 0001: Use PostgreSQL Over H2

## Status

Accepted

## Context

Tech Talent Pulse will store normalized source data, metric snapshots, and ETL run metadata. The project is intended to demonstrate production-minded data engineering practices, not only local application behavior.

H2 is convenient for lightweight development, but it can hide differences in SQL behavior, data types, indexing, constraints, and query planning that matter for an analytics-oriented application.

## Decision

Use PostgreSQL as the primary database for development, testing where realistic persistence behavior matters, and future deployment planning.

H2 should not be used as the primary persistence target for the project.

## Consequences

- Database behavior will better match production-style expectations.
- Integration tests can use Testcontainers to exercise PostgreSQL realistically.
- Local development will require a PostgreSQL-compatible environment once application code is introduced.
- The project will avoid H2-specific shortcuts that do not translate cleanly to PostgreSQL.
