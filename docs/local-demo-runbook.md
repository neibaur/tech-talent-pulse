# Local Demo Runbook

This runbook shows how a reviewer can start PostgreSQL, run the API locally, load explicit demo data,
trigger the local operational workflow, and verify the dashboard and admin readback endpoints.

## Prerequisites

- Java 21
- Maven
- Docker Desktop or another Docker environment with Compose support

## 1. Review Local Docker Database Defaults

The local demo workflow works without exporting database environment variables first. Docker Compose
and the Spring Boot `demo` profile share these local-only defaults:

- Database name: `tech_talent_pulse`
- Username: `tech_talent_pulse`
- Password: `tech_talent_pulse`

With the `demo` profile active, the Spring Boot app uses this local JDBC URL by default:

```text
jdbc:postgresql://localhost:5432/tech_talent_pulse
```

These defaults are for local Docker demo use only. They are not production credentials.

Optionally override the Docker database values in your local shell:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

If the Spring Boot app should use a different JDBC URL or separate app credentials, optionally
override these demo-specific environment variables:

- `TECH_TALENT_PULSE_DEMO_DATASOURCE_URL`
- `TECH_TALENT_PULSE_DEMO_DATASOURCE_USERNAME`
- `TECH_TALENT_PULSE_DEMO_DATASOURCE_PASSWORD`

## 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

Confirm the Compose file is valid:

```bash
docker compose config
```

## 3. Run The Spring Boot App Without Demo Data

The `demo` profile can start against local Docker PostgreSQL without seeding demo data:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## 4. Run The Spring Boot App With Demo Data

The demo data seeder is disabled by default. It runs only when the `demo` Spring profile is active
and `tech-talent-pulse.demo-data.enabled=true` is supplied explicitly.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments="--tech-talent-pulse.demo-data.enabled=true"
```

On startup, Flyway applies the production-safe schema migrations, then the demo seeder inserts
sample Stack Overflow-like raw question signals and runs the existing analytics transformation.
The seeded records are clearly marked in raw payload titles as demo sample data.

The seeder is idempotent for its provider IDs, so restarting the app with the `demo` profile should
not duplicate raw demo signals.

## 5. Call The API

Recent trend snapshots:

```bash
curl "http://localhost:8080/api/trends?limit=5"
```

Trend history for one tag:

```bash
curl "http://localhost:8080/api/trends/java?limit=3"
```

Summary:

```bash
curl "http://localhost:8080/api/trends/summary?limit=5"
```

Health check:

```bash
curl "http://localhost:8080/actuator/health"
```

## 6. Optional Manual Orchestration Triggers

Manual orchestration trigger endpoints are disabled by default. Enable them only for local/demo
operational use:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments="--tech-talent-pulse.admin.orchestration.enabled=true"
```

Run ingestion only:

```bash
curl -X POST http://localhost:8080/api/admin/orchestration/ingestion
```

Run transformation only:

```bash
curl -X POST http://localhost:8080/api/admin/orchestration/transformation
```

Run ingestion followed by transformation:

```bash
curl -X POST http://localhost:8080/api/admin/orchestration/pipeline
```

View recent ingestion run history:

```bash
curl "http://localhost:8080/api/admin/orchestration/runs?limit=10"
```

Example response shape:

```json
{
  "status": "COMPLETED",
  "provider": "STACK_OVERFLOW",
  "startedAt": "2026-01-05T12:00:00Z",
  "completedAt": "2026-01-05T12:00:30Z",
  "fetchedCount": 25,
  "persistedCount": 20,
  "duplicateCount": 5,
  "transformedSnapshotCount": 4,
  "message": "Ingestion and transformation completed."
}
```

Example history response shape:

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "provider": "STACK_OVERFLOW",
    "status": "COMPLETED",
    "startedAt": "2026-01-05T12:00:00Z",
    "completedAt": "2026-01-05T12:00:30Z",
    "errorMessage": null,
    "itemsRequested": 25,
    "itemsCaptured": 20,
    "itemsFetched": 22,
    "itemsDuplicateSkipped": 2
  }
]
```

After a trigger completes, verify the app and dashboard data:

```bash
curl "http://localhost:8080/actuator/health"
curl "http://localhost:8080/api/trends"
curl "http://localhost:8080/api/trends/summary"
```

The run history endpoint helps troubleshoot local ingestion runs by showing recent statuses,
timestamps, counts, and error messages without exposing raw payloads. These endpoints are local/demo
operational tools. They are not production-authenticated admin APIs.

## 7. Local Operational Smoke Validation

After PostgreSQL is running and the app starts with admin orchestration enabled, run the local smoke
script from a Linux shell or Git Bash on Windows:

```bash
bash scripts/smoke-local-demo.sh
```

The script assumes this app startup shape:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments="--tech-talent-pulse.admin.orchestration.enabled=true"
```

It checks:

- `GET /actuator/health`
- `GET /api/trends`
- `GET /api/trends/summary`
- `POST /api/admin/orchestration/ingestion`
- `POST /api/admin/orchestration/transformation`
- `POST /api/admin/orchestration/pipeline`
- `GET /api/admin/orchestration/runs?limit=10`

Expected result: every check prints `PASS` and the script exits with status `0`.

Optionally point the script at another local port:

```bash
BASE_URL="http://localhost:8081" bash scripts/smoke-local-demo.sh
```

Manual equivalent curl checks:

After the app starts with the `demo` profile and admin orchestration enabled, these commands should
return HTTP 200:

```bash
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/actuator/health"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends?limit=5"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/trends/summary?limit=5"
curl -o /dev/null -s -w "%{http_code}\n" -X POST "http://localhost:8080/api/admin/orchestration/ingestion"
curl -o /dev/null -s -w "%{http_code}\n" -X POST "http://localhost:8080/api/admin/orchestration/transformation"
curl -o /dev/null -s -w "%{http_code}\n" -X POST "http://localhost:8080/api/admin/orchestration/pipeline"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/admin/orchestration/runs?limit=10"
```

Troubleshooting:

- If the script cannot connect, confirm the app is running on the expected port.
- If admin endpoints return `404`, restart the app with
  `--tech-talent-pulse.admin.orchestration.enabled=true`.
- If startup fails on datasource or Flyway initialization, confirm PostgreSQL is running with
  `docker compose up -d postgres`.
- If local data looks stale, stop the app, reset the local Compose volume if appropriate, restart
  PostgreSQL, and rerun the demo seeding or orchestration flow.
- If an orchestration response has status `FAILED` while the HTTP status is `200`, inspect the app
  logs and recent run history. The smoke script validates the local API surface, not external source
  availability.

## 8. Phase 7 Operational Summary

Phase 7 is now a local operational demo layer over the existing backend:

- Phase 7A added synchronous orchestration for ingestion, transformation, and combined pipeline runs.
- Phase 7B added guarded manual trigger endpoints.
- Phase 7C added guarded recent ingestion run history readback.
- Phase 7D added a curl-based smoke validation script and final runbook polish.

Known limitations remain intentional for this portfolio increment:

- Admin endpoints are local/demo tools, not production-authenticated APIs.
- Orchestration remains synchronous and request-scoped.
- There is no queue, scheduler change, frontend, deployment infrastructure, or additional provider.
- History readback uses existing `ingestion_run` data rather than a new job-history model.

## 9. Run Validation

Run the full local validation gate:

```bash
mvn clean verify
```

This executes tests, Spotless checks, JaCoCo report generation, and the JaCoCo coverage gate.

View local coverage reports:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`
- CSV: `target/site/jacoco/jacoco.csv`

The current line coverage gate is `80%` at the Maven bundle level.

## Demo Data Safety

- Demo data is not loaded by the default profile.
- Demo data is not loaded by the `demo` profile unless `tech-talent-pulse.demo-data.enabled=true`
  is supplied explicitly.
- Demo datasource defaults are local-only and can be overridden through environment variables.
- Manual orchestration trigger endpoints are disabled unless explicitly enabled.
- Operational history readback is disabled unless manual orchestration endpoints are explicitly
  enabled.
- The smoke validation script calls only the local app base URL.
- No Flyway migration inserts demo data.
- Demo seeding and dashboard readback do not require external API calls; manual ingestion triggers
  may call the configured provider through the running app.
- No production secrets or credential values are stored in demo configuration.
- The demo seeder uses repository and transformation paths instead of raw SQL inserts.
