#!/usr/bin/env bash
# Self-test for gate.sh. The gate once printed a green banner through a failing step, so
# its own reporting is now something we check rather than trust: a gate you have not
# validated is not evidence. Runs gate.sh against a STUB gradlew in a throwaway dir, so
# it costs no Gradle daemons and no memory -- run it after any edit to gate.sh.
#
#   ./gate-selftest.sh

set -Eeuo pipefail
cd "$(dirname "$0")"
gate=$PWD/gate.sh

work=$(mktemp -d -t kground-gate-selftest-XXXXXX)
trap 'rm -rf "$work"' EXIT
cp "$gate" "$work/gate.sh"

# $1 = stub exit code
stub() { printf '#!/bin/sh\necho "stub gradlew: $*"\nexit %s\n' "$1" > "$work/gradlew"; chmod +x "$work/gradlew"; }

fails=0
# $1 = case name, $2 = expected exit, $3 = expected substring of the final verdict line, rest = gate args
expect() {
  local name=$1 want_rc=$2 want_line=$3; shift 3
  local out rc=0
  out=$(cd "$work" && PAUSE=0 ./gate.sh "$@" 2>&1) || rc=$?
  local last; last=$(printf '%s\n' "$out" | grep '^GATE: ' | tail -1 || true)
  if [[ $rc == "$want_rc" && $last == *"$want_line"* ]]; then
    echo "  PASS  $name"
  else
    echo "  FAIL  $name"
    echo "        expected exit $want_rc and a 'GATE: ' line containing <$want_line>"
    echo "        got      exit $rc and <${last:-(no GATE: line at all)}>"
    fails=$((fails + 1))
  fi
}

echo "gate.sh self-test"

stub 1
expect "a failing step is RED and names the step" 1 "RED (exit 1) -- step 'compile' failed" compile jvm

stub 0
# Every step "succeeds" but no apk exists, so the full run must still be RED -- this is the
# exact regression that made the gate untrustworthy: green banner, failing run.
expect "full run with no fresh apk is RED" 1 "RED (exit 1) -- template-andro ran but produced no fresh apk"

expect "steps that skip template-andro are GREEN" 0 "GREEN -- steps: compile jvm" compile jvm

expect "an unknown step name is RED, not GREEN" 2 "RED (exit 2) -- unknown step: bogus" bogus

# A green banner must never appear in a run that ends RED.
stub 0
out=$(cd "$work" && PAUSE=0 ./gate.sh 2>&1 || true)
if printf '%s' "$out" | grep -q 'GATE GREEN'; then
  echo "  FAIL  a RED run must not print the GATE GREEN banner"; fails=$((fails + 1))
else
  echo "  PASS  a RED run never prints the GATE GREEN banner"
fi

if [[ $fails -gt 0 ]]; then echo; echo "$fails self-test failure(s)" >&2; exit 1; fi
echo; echo "gate.sh self-test: all green"
