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
#   ./gate.sh native assemble # prewarm the heavy native compiles, then assemble
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

# Order matters: cheapest first, and everything expensive that `assemble` depends on is
# done as its own step BEFORE it, so `assemble` itself is left with jars and packaging.
# The breakdown comes from `./gradlew assemble -m` (dry run), which lists the real graph:
# 6x compileKotlinLinuxX64 + 6x compileTestKotlinLinuxX64 (native, the heaviest), 6x
# compileKotlinJs plus npm setup, 10x compileKotlinJvm, 6x metadata.
ALL_STEPS=(compile probes native-dist npm meta jvm js native native-test assemble
           template-basic template-full template-andro template-raw)

# The six modules with a linuxX64 target. Native compilation is the one place where doing
# all six in one invocation is noticeably heavier than doing them one at a time.
NATIVE_MODULES=(kground kground-io kgroundx kgroundx-io kommand-line kommand-samples)

# Each step prints one command per line; the runner executes them in order and pauses
# between them, so a step can be split into per-module invocations without extra plumbing.
step_cmds() {
  case "$1" in
    compile)       echo "./gradlew $GRADLE_FLAGS :template-logic:compileKotlin" ;;
    probes)        echo "./gradlew $GRADLE_FLAGS :kgroundx-experiments:probes" ;;
    native-dist)   echo "./gradlew $GRADLE_FLAGS downloadKotlinNativeDistribution" ;;
    npm)           echo "./gradlew $GRADLE_FLAGS kotlinKotlinNpmCachesSetup jsPackageJson jsPublicPackageJson" ;;
    meta)          echo "./gradlew $GRADLE_FLAGS compileCommonMainKotlinMetadata" ;;
    jvm)           echo "./gradlew $GRADLE_FLAGS compileKotlinJvm" ;;
    js)            echo "./gradlew $GRADLE_FLAGS compileKotlinJs" ;;
    native)        for m in "${NATIVE_MODULES[@]}"; do echo "./gradlew $GRADLE_FLAGS :$m:compileKotlinLinuxX64"; done ;;
    native-test)   for m in "${NATIVE_MODULES[@]}"; do echo "./gradlew $GRADLE_FLAGS :$m:compileTestKotlinLinuxX64"; done ;;
    assemble)      echo "./gradlew $GRADLE_FLAGS assemble" ;;
    template-*)    echo "./gradlew $GRADLE_FLAGS -p $1 assemble" ;;
    *)             return 1 ;;
  esac
}

mem() { free -h 2>/dev/null | awk '/^Mem:/ {printf "mem: %s used / %s total, %s available", $3, $2, $7}'; }

banner() { echo; echo "======================================================================"; echo "  $*"; echo "======================================================================"; }

if [[ ${1:-} == --list ]]; then printf '%s\n' "${ALL_STEPS[@]}"; exit 0; fi

steps=("$@")
if [[ ${#steps[@]} -eq 0 ]]; then steps=("${ALL_STEPS[@]}"); fi

# Timestamp reference, so "is the apk fresh?" compares against THIS run, not the clock.
gate_started=$(mktemp -t kground-gate-XXXXXX)

# NOTE: plain `[[ cond ]] && cmd` as a trailing statement is a set -e landmine -- when the
# condition is false the list returns 1. Everything below uses explicit `if`.
settle() {
  if [[ $STOP_DAEMONS == 1 ]]; then
    echo "-- stopping gradle daemons"
    ./gradlew --stop >/dev/null || true
  fi
  echo "-- settling ${PAUSE}s ($(mem))"
  sleep "$PAUSE"
}

for s in "${steps[@]}"; do
  mapfile -t cmds < <(step_cmds "$s") || true
  [[ ${#cmds[@]} -gt 0 ]] || { echo "unknown step: $s (try --list)" >&2; exit 2; }
  banner "STEP $s   ($((${#cmds[@]})) invocation(s))   $(date +%H:%M:%S)   $(mem)"
  for cmd in "${cmds[@]}"; do
    echo "+ $cmd"
    start=$SECONDS
    # No pipe into tail here: the point is to see it work, and a pipe would hide the exit code.
    if $cmd; then
      echo "-- OK in $((SECONDS - start))s"
    else
      echo "!! FAILED after $((SECONDS - start))s: $cmd" >&2
      echo "!! stopping at step '$s' -- nothing below was run." >&2
      exit 1
    fi
    if [[ "$cmd" != "${cmds[-1]}" ]]; then settle; fi
  done
  if [[ "$s" != "${steps[-1]}" ]]; then settle; fi
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
