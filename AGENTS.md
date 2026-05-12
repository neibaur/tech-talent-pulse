# AGENTS.md

This file defines repository expectations for human contributors and AI-assisted development agents working on Tech Talent Pulse.

## Project Context

Tech Talent Pulse is a Java 21 Spring Boot ETL and dashboard portfolio project. It will use Maven, PostgreSQL, and a lightweight hexagonal architecture to transform public technology-signal data into recruiter-friendly trend intelligence.

The repository is currently in Phase 8D. Stack Overflow raw ingestion, daily analytics transformation code, read-only dashboard API endpoints, read-only advanced analytics endpoints, read-only tag comparison analytics, analytics demo documentation, architecture-readiness documentation, static API documentation, explicit local demo data support, synchronous ingestion/transformation orchestration foundation code, opt-in local/demo manual orchestration trigger endpoints, opt-in local/demo ingestion run history readback, and local smoke validation scripting are allowed. Dashboard UI, GitHub ingestion, Kafka/event streaming, authentication/security stack, deployment infrastructure, production admin APIs, new orchestration history tables, queues, scheduler changes, predictive scoring, and additional external clients should wait for later scoped phases.

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
- Prefer Spring `RestClient` for synchronous HTTP integrations.
- Keep raw ingestion separate from analytics transformations.
- Keep analytics transformations focused on explainable, dashboard-ready metrics.
- Keep dashboard API controllers thin and avoid exposing JPA entities directly.

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

Current executable validation commands:

- `mvn clean verify`
- `mvn test`
- `docker compose config`

`mvn clean verify` runs the Maven build, tests, Spotless checks, JaCoCo report generation, and the JaCoCo coverage gate.

JaCoCo report outputs:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`

The current coverage gate requires at least 80% line coverage at the bundle level. Treat 80% as the near-term minimum and improve branch coverage over time without excluding meaningful application behavior.

PostgreSQL repository integration tests use Testcontainers and should run in Docker-enabled environments. They may be skipped locally when Docker is unavailable, but CI should provide Docker so those tests exercise PostgreSQL.

GitHub Actions validation should remain aligned with local validation:

- `CI / build-and-validate` runs `mvn clean verify` and `docker compose config`.
- `CI / build-and-validate` uploads JaCoCo reports and failed test reports as artifacts.
- `CI / secret-scan` runs Gitleaks without repository-managed secrets.
- `CodeQL / analyze-java` runs Java CodeQL analysis with a manual Maven compile.

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
- Keep Phase 2 ingestion limited to raw Stack Overflow question capture unless a later task explicitly expands scope.
- Keep Phase 3 transformations limited to Stack Overflow daily tag-level trend metrics unless a later task explicitly expands scope.
- Keep Phase 5 API work limited to read-only dashboard metrics unless a later task explicitly expands scope.
- Keep Phase 6 work limited to API documentation, local demo data, and reviewer runbook improvements unless a later task explicitly expands scope.
- Keep Phase 7A work limited to orchestration foundation, operational result objects, ingestion run status/count tracking, and logging unless a later task explicitly expands scope.
- Keep Phase 7B work limited to opt-in local/demo manual orchestration trigger endpoints unless a later task explicitly expands scope.
- Keep Phase 7C work limited to opt-in local/demo operational history readback from existing `ingestion_run` data unless a later task explicitly expands scope.
- Keep Phase 7D work limited to local smoke validation scripting and final operational documentation polish unless a later task explicitly expands scope.
- Keep Phase 8A work limited to read-only trend delta and rising technology analytics over existing `technology_trend_snapshot` data unless a later task explicitly expands scope.
- Keep Phase 8B work limited to read-only tag comparison analytics over existing `technology_trend_snapshot` data unless a later task explicitly expands scope.
- Keep Phase 8C work limited to analytics demo readiness, local smoke validation coverage, and documentation polish unless a later task explicitly expands scope.
- Keep Phase 8D work limited to documentation and architecture-readiness context unless a later task explicitly expands scope.
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
