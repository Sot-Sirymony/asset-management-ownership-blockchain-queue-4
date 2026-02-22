#!/usr/bin/env bash
# Resolve all issues after running tests (e2e or UI validation).
# Run from repo root: ./after-test.sh [--docker] [--artifacts] [--all]
#
# Options:
#   (none)    Free ports 3000 and 8081 only.
#   --docker  Also stop API/Postgres Docker services (if started for tests).
#   --artifacts  Also remove Playwright test-results and report folders.
#   --all     Same as --docker and --artifacts.

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
UI_DIR="$ROOT_DIR/ownership-ui-master"
API_DIR="$ROOT_DIR/ownership-api-master"

DO_DOCKER=false
DO_ARTIFACTS=false

for arg in "$@"; do
  case "$arg" in
    --docker)   DO_DOCKER=true ;;
    --artifacts) DO_ARTIFACTS=true ;;
    --all)      DO_DOCKER=true; DO_ARTIFACTS=true ;;
    -h|--help)
      echo "Usage: $0 [--docker] [--artifacts] [--all]"
      echo "  --docker    Stop API/Postgres Docker services (ownership-api-master)."
      echo "  --artifacts Remove Playwright test-results/ and playwright-report/."
      echo "  --all       Do both."
      exit 0
      ;;
  esac
done

echo "=========================================="
echo "After-test cleanup"
echo "=========================================="

# 1. Free ports 3000 (UI) and 8081 (API)
for port in 3000 8081; do
  if lsof -ti :"$port" >/dev/null 2>&1; then
    echo "Freed port $port"
    lsof -ti :"$port" | xargs kill -9 2>/dev/null || true
  else
    echo "Port $port was already free"
  fi
done

# 2. Optional: stop Docker services used by API
if [ "$DO_DOCKER" = true ] && [ -f "$API_DIR/docker-compose.yml" ]; then
  echo ""
  echo "Stopping API Docker services..."
  docker compose -f "$API_DIR/docker-compose.yml" stop api postgres otel-collector 2>/dev/null || true
  echo "Done."
fi

# 3. Optional: remove Playwright artifacts
if [ "$DO_ARTIFACTS" = true ] && [ -d "$UI_DIR" ]; then
  echo ""
  for dir in test-results playwright-report; do
    if [ -d "$UI_DIR/$dir" ]; then
      rm -rf "$UI_DIR/$dir"
      echo "Removed $UI_DIR/$dir"
    fi
  done
fi

echo ""
echo "Cleanup finished. Ports 3000 and 8081 are free."
