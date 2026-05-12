# API Reference

Tech Talent Pulse exposes read-only dashboard endpoints over daily technology trend snapshots.
The current API is intentionally lightweight and does not add OpenAPI or Swagger dependencies.
Static documentation keeps Phase 6 focused on local demo readiness without adding runtime surface area.

Base URL for local development:

```bash
http://localhost:8080
```

## Limits

All endpoints return HTTP 200 when the application is running and the database is reachable.
When no trend data exists, collection fields are empty.

The optional `limit` query parameter is normalized by the application:

- Missing, zero, or negative values use the endpoint default.
- Values above `500` are capped at `500`.
- Snapshot endpoints default to `50`.
- Summary top-tag results default to `5`.
- Advanced analytics endpoints default to `25` and cap at `100`.

## GET /api/trends

Returns recent trend snapshots ordered by newest snapshot date first, then tag.

Query parameters:

- `limit`: optional integer. Defaults to `50`; maximum `500`.

Example request:

```bash
curl "http://localhost:8080/api/trends?limit=5"
```

Example response:

```json
[
  {
    "tag": "docker",
    "provider": "STACK_OVERFLOW",
    "snapshotDate": "2026-05-10",
    "signalCount": 1,
    "averageScore": 8.0,
    "averageAnswerCount": 3.0
  },
  {
    "tag": "java",
    "provider": "STACK_OVERFLOW",
    "snapshotDate": "2026-05-10",
    "signalCount": 1,
    "averageScore": 14.0,
    "averageAnswerCount": 5.0
  }
]
```

## GET /api/trends/{tag}

Returns trend history for one technology tag ordered by newest snapshot date first.
Tag matching is case-insensitive.

Path parameters:

- `tag`: technology tag such as `java`, `spring-boot`, `postgresql`, `docker`, or `kubernetes`.

Query parameters:

- `limit`: optional integer. Defaults to `50`; maximum `500`.

Example request:

```bash
curl "http://localhost:8080/api/trends/java?limit=3"
```

Example response:

```json
[
  {
    "tag": "java",
    "provider": "STACK_OVERFLOW",
    "snapshotDate": "2026-05-10",
    "signalCount": 1,
    "averageScore": 14.0,
    "averageAnswerCount": 5.0
  },
  {
    "tag": "java",
    "provider": "STACK_OVERFLOW",
    "snapshotDate": "2026-05-09",
    "signalCount": 1,
    "averageScore": 11.0,
    "averageAnswerCount": 4.0
  }
]
```

## GET /api/trends/summary

Returns the most recent snapshot date and the top tags by total signal count.

Query parameters:

- `limit`: optional integer. Defaults to `5`; maximum `500`.

Example request:

```bash
curl "http://localhost:8080/api/trends/summary?limit=5"
```

Example response:

```json
{
  "mostRecentSnapshotDate": "2026-05-10",
  "topTags": [
    {
      "tag": "java",
      "signalCount": 3
    },
    {
      "tag": "spring-boot",
      "signalCount": 2
    }
  ]
}
```

## Notes

- Responses are DTOs, not JPA entities.
- Current data comes from Stack Overflow raw question signals transformed into daily snapshots.
- Authentication, a public deployment, dashboard UI, and additional ingestion providers remain out of scope.

## GET /api/analytics/trends/deltas

Returns per-tag movement by comparing the latest snapshot date with the previous available snapshot
date.

Query parameters:

- `limit`: optional integer. Defaults to `25`; maximum `100`.

Calculation:

- `absoluteDelta = currentSignalCount - previousSignalCount`
- `percentChange = absoluteDelta / previousSignalCount * 100`
- `percentChange` is `null` when previous data is missing or the previous count is `0`.
- `rankMovement = previousRank - currentRank`, so positive values mean the tag moved up.

Example request:

```bash
curl "http://localhost:8080/api/analytics/trends/deltas?limit=10"
```

Example response:

```json
[
  {
    "tag": "java",
    "currentSnapshotDate": "2026-05-10",
    "previousSnapshotDate": "2026-05-09",
    "currentSignalCount": 20,
    "previousSignalCount": 10,
    "absoluteDelta": 10,
    "percentChange": 100.0,
    "currentRank": 1,
    "previousRank": 2,
    "rankMovement": 1
  }
]
```

## GET /api/analytics/trends/rising

Returns technologies from the latest snapshot date that show positive signal growth or positive rank
movement compared with the previous snapshot date.

Query parameters:

- `limit`: optional integer. Defaults to `25`; maximum `100`.

Sorting:

1. Highest absolute signal delta.
2. Highest positive rank movement.
3. Highest current signal count.
4. Tag name ascending for deterministic ties.

Example request:

```bash
curl "http://localhost:8080/api/analytics/trends/rising?limit=5"
```

Example response:

```json
[
  {
    "tag": "postgresql",
    "currentSnapshotDate": "2026-05-10",
    "previousSnapshotDate": "2026-05-09",
    "currentSignalCount": 16,
    "previousSignalCount": 6,
    "absoluteDelta": 10,
    "percentChange": 166.6666666667,
    "currentRank": 2,
    "previousRank": 3,
    "rankMovement": 1
  }
]
```

Limitations:

- Phase 8A compares only the latest and immediately previous snapshot dates.
- Missing prior tag data does not produce a percent change because the baseline is not meaningful.
- These analytics are explanatory indicators, not hiring predictions.

## Optional Local Admin Orchestration

Phase 7B adds local/demo manual orchestration triggers. They are disabled by default and require
`tech-talent-pulse.admin.orchestration.enabled=true`.

Routes:

- `POST /api/admin/orchestration/ingestion`
- `POST /api/admin/orchestration/transformation`
- `POST /api/admin/orchestration/pipeline`
- `GET /api/admin/orchestration/runs?limit=10`

Trigger endpoints return status, provider, timestamps, ingestion counts, transformed snapshot count,
and a short message. Run history returns recent `ingestion_run` records as DTOs with safe limit
handling. These endpoints are intended for local operational demos, not production exposure.

## Local Smoke Validation

When the app is running locally with admin orchestration enabled, validate the demo API surface with:

```bash
bash scripts/smoke-local-demo.sh
```

The script checks the health endpoint, read-only dashboard endpoints, manual orchestration triggers,
and recent run history. It uses curl only and does not require additional dependencies.
