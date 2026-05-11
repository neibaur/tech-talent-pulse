# ADR 0002: Use Lightweight Hexagonal Architecture

## Status

Accepted

## Context

Tech Talent Pulse will integrate external APIs, transform data, persist metrics, and present dashboard-ready insights. These responsibilities should be separated enough to keep the system testable and adaptable.

At the same time, this is a portfolio project. The architecture should stay understandable and avoid excessive abstraction.

## Decision

Use a lightweight hexagonal architecture.

Core transformation and domain logic should be isolated from external API clients, persistence frameworks, and presentation concerns. Adapters should handle infrastructure details such as GitHub API access, PostgreSQL persistence, and dashboard/API delivery.

## Consequences

- Domain and transformation logic can be tested without live API calls or framework-heavy setup.
- Infrastructure details can change with less impact on core logic.
- Contributors must maintain clear package and responsibility boundaries.
- The project should avoid abstracting every class by default; abstractions should serve real testability or dependency-direction needs.
