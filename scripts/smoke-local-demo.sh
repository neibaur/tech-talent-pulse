#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
CURL_CONNECT_TIMEOUT="${CURL_CONNECT_TIMEOUT:-5}"
CURL_MAX_TIME="${CURL_MAX_TIME:-120}"

pass() {
  printf 'PASS %s\n' "$1"
}

fail() {
  printf 'FAIL %s\n' "$1" >&2
  exit 1
}

require_curl() {
  if ! command -v curl >/dev/null 2>&1; then
    fail "curl is required to run the local demo smoke validation"
  fi
}

check_status() {
  local name="$1"
  local method="$2"
  local path="$3"
  local expected_status="$4"
  local response_file
  local status

  response_file="$(mktemp)"
  if ! status="$(
    curl \
      --silent \
      --show-error \
      --connect-timeout "$CURL_CONNECT_TIMEOUT" \
      --max-time "$CURL_MAX_TIME" \
      --output "$response_file" \
      --write-out '%{http_code}' \
      --request "$method" \
      "${BASE_URL}${path}"
  )"; then
    rm -f "$response_file"
    fail "${name} could not reach ${BASE_URL}${path}. Is the app running?"
  fi

  if [[ "$status" != "$expected_status" ]]; then
    printf 'Response body:\n' >&2
    sed -n '1,20p' "$response_file" >&2
    rm -f "$response_file"
    fail "${name} expected HTTP ${expected_status} but received ${status}"
  fi

  rm -f "$response_file"
  pass "${name} returned HTTP ${status}"
}

main() {
  require_curl

  printf 'Running local demo smoke validation against %s\n' "$BASE_URL"
  printf 'Assumption: the app is running with tech-talent-pulse.admin.orchestration.enabled=true\n'

  check_status "health" "GET" "/actuator/health" "200"
  check_status "trends" "GET" "/api/trends" "200"
  check_status "trend summary" "GET" "/api/trends/summary" "200"
  check_status "trend deltas" "GET" "/api/analytics/trends/deltas" "200"
  check_status "rising trends" "GET" "/api/analytics/trends/rising" "200"
  check_status "tag comparison" "GET" "/api/analytics/trends/compare?tags=java,python" "200"
  check_status "ingestion trigger" "POST" "/api/admin/orchestration/ingestion" "200"
  check_status "transformation trigger" "POST" "/api/admin/orchestration/transformation" "200"
  check_status "pipeline trigger" "POST" "/api/admin/orchestration/pipeline" "200"
  check_status "run history" "GET" "/api/admin/orchestration/runs?limit=10" "200"

  printf 'Local demo smoke validation completed successfully.\n'
}

main "$@"
