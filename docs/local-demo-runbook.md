# Local Demo Runbook

This runbook shows how a reviewer can start PostgreSQL, run the API locally, load explicit demo data,
trigger the local operational workflow, and verify dashboard, analytics, and admin readback
endpoints.

## Prerequisites

- Java 21
- Maven
- Node.js 20 or newer
- pnpm
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

To run the complete analytics demo flow, start the app with demo data and guarded admin
orchestration enabled:

```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=demo \
  -Dspring-boot.run.arguments="--tech-talent-pulse.demo-data.enabled=true --tech-talent-pulse.admin.orchestration.enabled=true"
```

Demo data and transformation are required before the dashboard and analytics endpoints can return
non-empty results. Empty arrays or empty comparison histories are valid when no transformed
`technology_trend_snapshot` rows exist yet.

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

Trend deltas:

```bash
curl "http://localhost:8080/api/analytics/trends/deltas?limit=10"
```

Rising technologies:

```bash
curl "http://localhost:8080/api/analytics/trends/rising?limit=5"
```

Tag comparison:

```bash
curl "http://localhost:8080/api/analytics/trends/compare?tags=Java,PYTHON,java"
```

Use URL-safe query strings for tag comparison. Do not rely on unencoded spaces inside URLs.

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
curl "http://localhost:8080/api/analytics/trends/deltas"
curl "http://localhost:8080/api/analytics/trends/rising"
curl "http://localhost:8080/api/analytics/trends/compare?tags=java,python"
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
- `GET /api/analytics/trends/deltas`
- `GET /api/analytics/trends/rising`
- `GET /api/analytics/trends/compare?tags=java,python`
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
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/analytics/trends/deltas"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/analytics/trends/rising"
curl -o /dev/null -s -w "%{http_code}\n" "http://localhost:8080/api/analytics/trends/compare?tags=java,python"
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
- If dashboard or analytics endpoints return empty arrays or empty histories, run demo seeding or
  trigger transformation/pipeline so transformed snapshots exist.
- If an orchestration response has status `FAILED` while the HTTP status is `200`, inspect the app
  logs and recent run history. The smoke script validates the local API surface, not external source
  availability.

## 8. Run The Astro Frontend

The Phase 9A frontend is a lightweight Astro app in `frontend/`. It runs independently from the
Spring Boot backend and defaults to the local API at `http://localhost:8080`.

Start the backend first with demo data and guarded orchestration enabled:

```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=demo \
  -Dspring-boot.run.arguments="--tech-talent-pulse.demo-data.enabled=true --tech-talent-pulse.admin.orchestration.enabled=true"
```

Then start the frontend in another terminal:

```bash
cd frontend
pnpm install
pnpm run dev
```

Open:

```text
http://localhost:4321
```

The dashboard calls:

- `GET /api/analytics/trends/rising`
- `GET /api/trends/summary`
- `GET /api/analytics/trends/compare?tags=java,python,postgresql`

Optional frontend API override:

```bash
PUBLIC_TECH_TALENT_PULSE_API_URL="http://localhost:8080" pnpm run dev
```

The backend allows the Astro dev server through a local-development CORS setting:

- `TECH_TALENT_PULSE_WEB_CORS_ALLOWED_ORIGINS`, default `http://localhost:4321`

Frontend troubleshooting:

- If the dashboard says the backend API is unavailable, confirm the Spring Boot app is running and
  `curl "http://localhost:8080/actuator/health"` returns `200`.
- If the browser console shows CORS errors, confirm the frontend is running from
  `http://localhost:4321` or set `TECH_TALENT_PULSE_WEB_CORS_ALLOWED_ORIGINS` to the active local
  frontend origin.
- If cards or charts are empty, run demo seeding or trigger the pipeline so transformed snapshots
  exist. Empty analytics data is valid when the database has no snapshot history.
- If the Astro dev server uses another port, update the backend CORS origin before refreshing the
  browser.

## 9. Troubleshooting Quick Reference

| Symptom | Likely Root Cause | Quick Validation | Recommended Fix |
| --- | --- | --- | --- |
| CI or local startup fails with missing tables or schema validation errors. | Flyway migration files were not committed or were accidentally ignored. | Check `src/main/resources/db/migration` and run `mvn clean verify`. | Restore/commit migration files and keep Flyway as the schema source of truth. |
| Startup fails with datasource or Flyway JDBC errors. | Datasource URL is missing, malformed, or resolves from an unresolved placeholder. | Check `src/main/resources/application.yml`, `src/main/resources/application-demo.yml`, and startup logs. | Use a concrete JDBC URL fallback such as the local Docker PostgreSQL URL and avoid unresolved nested placeholders. |
| PostgreSQL authentication fails for a literal placeholder value. | Username/password fallback resolved to text like an environment variable placeholder. | Inspect the logged datasource username and demo profile config. | Provide concrete local demo fallbacks matching Docker Compose, with environment variables only as overrides. |
| Dashboard or analytics endpoints return empty arrays. | No transformed `technology_trend_snapshot` rows exist yet. | Call `/api/trends`, `/api/analytics/trends/deltas`, or inspect transformation logs. | Run demo seeding or trigger transformation/pipeline with guarded admin endpoints enabled. |
| Admin orchestration endpoints return `404`. | Guard property is disabled. | Check startup command for `tech-talent-pulse.admin.orchestration.enabled=true`. | Restart with the property enabled for local/demo operational use. |
| Compare endpoint fails when a URL contains spaces. | Client/server URL parsing rejected unencoded whitespace before application normalization. | Retry with `tags=Java,PYTHON,java`. | Use URL-safe query strings or encode spaces if a client emits them. |
| Smoke script fails to connect. | App is not running on the expected base URL. | Call `curl "http://localhost:8080/actuator/health"`. | Start the app or set `BASE_URL` to the active local port. |
| Frontend shows an API unavailable message. | Backend is stopped, API URL is wrong, or local CORS origin does not match. | Check the browser console and call `/actuator/health`. | Start the backend, set `PUBLIC_TECH_TALENT_PULSE_API_URL`, or align `TECH_TALENT_PULSE_WEB_CORS_ALLOWED_ORIGINS`. |

## 10. Phase 7 Operational Summary

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

## 11. Phase 8 Analytics Summary

Phase 8 is now complete as a backend/API-only analytics layer:

- Phase 8A added trend deltas and rising technology endpoints.
- Phase 8B added chart-ready tag comparison analytics.
- Phase 8C extended smoke validation and polished analytics demo documentation.

Phase 9 can focus on frontend visualization over these existing DTO responses. Operational/admin
endpoints remain guarded and are still local/demo tooling, not production-authenticated admin APIs.

## 12. Phase 9 Frontend Summary

Phase 9 begins frontend visualization without changing backend API contracts:

- Astro 6 provides the static-friendly frontend foundation.
- React is limited to the dashboard/chart island.
- Recharts renders comparison history from the existing chart-ready API response.
- Phase 9B improves recruiter/demo presentation with summary cards, clearer metric labels,
  improved empty/offline states, and a more readable comparison chart.
- The frontend is not authenticated and should not be treated as production hosting.

## 13. Run Validation

Run the full local validation gate:

```bash
mvn clean verify
```

This executes tests, Spotless checks, JaCoCo report generation, and the JaCoCo coverage gate.

Run the frontend build check:

```bash
cd frontend
pnpm install
pnpm astro check
pnpm run build
```

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
