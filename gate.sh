#!/usr/bin/env bash
# Gate for the build-logic-context-params branch: prove template-logic still works.
#
# Why a script and not a one-liner: running probes + KGround assemble + four template
# assembles back to back pins several Gradle and Kotlin daemons at once and chokes a
# laptop with little RAM. This runs ONE step at a time, smallest first, and pauses in
# between so memory can settle and you can Ctrl-C at a sane boundary.
#
# Usage:
#   ./gate.sh                 # every step, in order, smallest first
#   ./gate.sh probes          # just one step (any step name below)
#   ./gate.sh compile probes  # a few, in the order given
#   PAUSE=60 ./gate.sh        # longer settle between steps (default 20s)
#   STOP_DAEMONS=1 ./gate.sh  # stop Gradle daemons after every step (slowest, leanest)
#   ./gate.sh --list          # show step names and exit

set -euo pipefail
cd "$(dirname "$0")"

PAUSE=${PAUSE:-20}
STOP_DAEMONS=${STOP_DAEMONS:-0}
# One worker keeps peak memory down; this gate is about correctness, not wall time.
GRADLE_FLAGS=${GRADLE_FLAGS:---max-workers=1}

ALL_STEPS=(compile probes assemble template-basic template-full template-andro template-raw)

step_cmd() {
  case "$1" in
    compile)       echo "./gradlew $GRADLE_FLAGS :template-logic:compileKotlin" ;;
    probes)        echo "./gradlew $GRADLE_FLAGS :kgroundx-experiments:probes" ;;
    assemble)      echo "./gradlew $GRADLE_FLAGS assemble" ;;
    template-*)    echo "./gradlew $GRADLE_FLAGS -p $1 assemble" ;;
    *)             return 1 ;;
  esac
}

mem() { free -h 2>/dev/null | awk '/^Mem:/ {printf "mem: %s used / %s total, %s available", $3, $2, $7}'; }

banner() { echo; echo "======================================================================"; echo "  $*"; echo "======================================================================"; }

if [[ ${1:-} == --list ]]; then printf '%s\n' "${ALL_STEPS[@]}"; exit 0; fi

steps=("$@"); [[ ${#steps[@]} -eq 0 ]] && steps=("${ALL_STEPS[@]}")

# Timestamp reference, so "is the apk fresh?" compares against THIS run, not the clock.
gate_started=$(mktemp -t kground-gate-XXXXXX)

for s in "${steps[@]}"; do
  cmd=$(step_cmd "$s") || { echo "unknown step: $s (try --list)" >&2; exit 2; }
  banner "STEP $s   $(date +%H:%M:%S)   $(mem)"
  echo "+ $cmd"
  start=$SECONDS
  # No pipe into tail here: the point is to see it work, and a pipe would hide the exit code.
  if $cmd; then
    echo "-- $s OK in $((SECONDS - start))s"
  else
    echo "!! $s FAILED after $((SECONDS - start))s -- stopping here, nothing below was run." >&2
    exit 1
  fi
  [[ $STOP_DAEMONS == 1 ]] && { echo "-- stopping gradle daemons"; ./gradlew --stop >/dev/null || true; }
  if [[ "$s" != "${steps[-1]}" ]]; then
    echo "-- settling ${PAUSE}s before the next step ($(mem))"
    sleep "$PAUSE"
  fi
done

banner "GATE GREEN   $(date +%H:%M:%S)   $(mem)"
echo "steps run: ${steps[*]}"

# Only report the apk when THIS run built it. Printing it unconditionally would show a
# stale artifact from an earlier run -- a success signal that cannot fail is not evidence.
apk=template-andro/template-andro-app/build/outputs/apk/debug/template-andro-app-debug.apk
if [[ " ${steps[*]} " == *" template-andro "* ]]; then
  if [[ -f $apk && $apk -nt $gate_started ]]; then
    echo "apk: $(find "$apk" -printf '%TY-%Tm-%Td %TH:%TM %p\n')"
  else
    echo "!! template-andro ran but produced no fresh apk: $apk" >&2
    exit 1
  fi
fi
rm -f "$gate_started"
