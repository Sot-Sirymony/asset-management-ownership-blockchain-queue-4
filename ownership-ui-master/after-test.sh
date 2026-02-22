#!/usr/bin/env bash
# Run the repo-root after-test script (so you can run ./after-test.sh from here).
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
exec "$ROOT/after-test.sh" "$@"
