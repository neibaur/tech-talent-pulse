# AGENTS.md

This file defines repository expectations for human contributors and AI-assisted development agents working on Tech Talent Pulse.

## Project Context

Tech Talent Pulse is a Java 21 Spring Boot ETL and dashboard portfolio project. It will use Maven, PostgreSQL, and a lightweight hexagonal architecture to transform public technology-signal data into recruiter-friendly trend intelligence.

The repository is currently in Phase 1. Application foundation code is allowed, but GitHub ingestion, ETL business logic, dashboard UI, external API clients, entities, repositories, and services should wait for later scoped phases.

## Atomic Commit Policy

Propose and commit changes in small, logical increments. Each commit should describe one coherent improvement, such as documentation foundation, build skeleton, ingestion adapter, metric model, or test coverage.

Avoid large monolithic changes that mix unrelated documentation, infrastructure, application code, and formatting updates.

## Security First

Never generate placeholder secrets, fake tokens, hardcoded credentials, example passwords, or mock API keys in code, tests, documentation, scripts, screenshots, or comments.

Use environment variable names only when describing configuration. Do not include real or fake secret values.

## Secret Handling Rules

- Reference required secrets by environment variable name only.
- Do not commit `.env`, `.env.*`, local shell profiles, generated credentials, or exported secret files.
- Do not include example credential values in documentation.
- Do not add secrets to test fixtures, sample JSON, screenshots, logs, or generated reports.
- Prefer explicit documentation that tells contributors where a value comes from, not what its value should be.

## Safe And Protected Files

Treat these files and paths with extra care:

- `LICENSE`
- `README.md`
- `AGENTS.md`
- `.github/CODEOWNERS`
- `.github/dependabot.yml`
- `.github/pull_request_template.md`
- `docs/adr/`
- Future Maven build files such as `pom.xml`
- Future deployment, CI, or infrastructure files

Do not rewrite governance or decision records casually. Prefer additive updates or new ADRs when decisions change.

## Coding Standards

When application code is introduced:

- Use Java 21 language features conservatively and clearly.
- Follow Maven directory conventions: `src/main/java`, `src/main/resources`, `src/test/java`, and `src/test/resources`.
- Keep Spring Boot framework code at the edges where practical.
- Favor explicit domain names over vague utility abstractions.
- Keep adapters, application services, and domain concepts separated.
- Prefer constructor injection for Spring-managed dependencies.
- Avoid hidden network calls in unit tests.

## Linter Enforcement Expectations

Formatting, static analysis, and dependency checks should be automated once the Maven build exists.

Planned enforcement areas include:

- Java formatting.
- Unit and integration test execution.
- Static analysis for common defects.
- Dependency vulnerability review.
- Documentation and Markdown consistency where practical.

## Testing Expectations

When implementation begins:

- Add focused unit tests for domain logic and transformation rules.
- Add integration tests for persistence and external adapter boundaries where useful.
- Use Testcontainers when database-backed behavior needs realistic PostgreSQL coverage.
- Keep tests deterministic and independent from live public APIs.
- Use fixtures that contain no real or fake secrets.

## Validation Commands

Phase 1 executable validation commands:

- `mvn clean verify`
- `mvn test`
- `docker compose config`

`mvn clean verify` runs the Maven build, tests, Spotless checks, and JaCoCo report generation.

## Definition Of Done

A change is done when:

- It satisfies the stated scope without unrelated edits.
- Documentation is updated when behavior, decisions, or contributor workflows change.
- Security and secret-handling rules are followed.
- Tests are added or updated when implementation behavior changes.
- Planned or available validation commands have been run, or skipped with a clear reason.
- The PR description includes testing evidence or explains why testing is not applicable.

## AI-Assisted Development Rules For Codex

Codex should:

- Read the existing repository context before editing.
- Keep changes scoped to the user request.
- Preserve user changes and avoid reverting unrelated work.
- Keep Phase 1 application code limited to the Spring Boot foundation unless a later task explicitly expands scope.
- Use Maven assumptions for future Java validation and examples.
- Use the documented Maven and Docker Compose validation commands when relevant.
- Use environment variable names only when discussing secrets.
- Prefer documentation updates that are concise, professional, and portfolio-ready.
- Explain any skipped validation clearly.

## PR Expectations

Pull requests should:

- Have a focused title and summary.
- Explain what changed and why.
- Include validation evidence or state why validation is not applicable.
- Call out documentation, governance, security, and testing impacts.
- Keep screenshots or dashboard evidence attached when UI behavior changes in future phases.
