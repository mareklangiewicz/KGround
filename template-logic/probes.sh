#!/usr/bin/env bash
# Executable probes for the context-parameter claims in migration-status.md.
#
# Half of those claims are about what does NOT compile, so they cannot be
# expressed as ordinary unit tests. Each probe compiles real code with real
# Gradle and asserts the exact outcome.
#
# Usage:  template-logic/probes.sh          # run all
#         template-logic/probes.sh 3        # run one by number
#
# Exit code 0 = every claim still holds.
#
# Residue: the template-logic side is a scratch FILE that is created and
# deleted, never an edit to a real source. Only the consumer build script is
# appended to, and that is backed up, restored after every probe, and verified
# against git at the end. The script refuses to start if either target is
# already dirty, so a polluted tree can never be captured as the "clean" backup.
#
# Known limit: a hard Ctrl-C mid-Gradle can still orphan the scratch file --
# measured, and the trap ordering was not fully pinned down. That is why the
# guards below exist: the leftover is untracked, distinctly named, and the next
# run refuses loudly with the command to clear it, rather than silently baking
# it into a backup. Pass --clean to clear any leftovers and exit.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRATCH="$ROOT/template-logic/src/main/kotlin/pl/mareklangiewicz/templatelogic/ZzProbeScratch.kt"
SCRIPT="$ROOT/kground/build.gradle.kts"
SCRIPT_REL="kground/build.gradle.kts"
ONLY="${1:-}"

cd "$ROOT" || exit 1

if [[ $ONLY == --clean ]]; then
  rm -f "$SCRATCH"
  git checkout -- "$SCRIPT_REL" 2>/dev/null
  echo "cleared probe leftovers"; exit 0
fi

# Never back up a dirty file: that would "restore" pollution later.
if ! git diff --quiet -- "$SCRIPT_REL"; then
  echo "refusing to run: $SCRIPT_REL has uncommitted changes" >&2
  echo "if those are probe leftovers: template-logic/probes.sh --clean" >&2; exit 2
fi
if [[ -e $SCRATCH ]]; then
  echo "refusing to run: stale scratch from an interrupted run: $SCRATCH" >&2
  echo "clear it with: template-logic/probes.sh --clean" >&2; exit 2
fi

BAK="$(mktemp -d)"
cp "$SCRIPT" "$BAK/script.kts"
restore() { cp "$BAK/script.kts" "$SCRIPT"; rm -f "$SCRATCH"; }
cleanup() { restore; rm -rf "$BAK"; }
# EXIT alone is NOT enough: killed mid-Gradle, bash can die from the signal
# without running the EXIT trap, leaving probe code behind. Trap signals too.
trap cleanup EXIT
trap 'cleanup; exit 130' INT TERM HUP

pass=0; fail=0
GRADLE=(./gradlew --console=plain -q :kground:tasks)

report() {
  if [[ "$2" == ok ]]; then printf '  \033[32mPASS\033[0m  %s\n' "$1"; pass=$((pass+1))
  else printf '  \033[31mFAIL\033[0m  %s\n        %s\n' "$1" "$3"; fail=$((fail+1)); fi
}

run_build() { "${GRADLE[@]}" 2>&1; }

# scratch <<'KT' ... KT  -- write the template-logic side of a probe
scratch() {
  { echo "package pl.mareklangiewicz.templatelogic"
    echo "import pl.mareklangiewicz.deps.*"
    cat
  } >"$SCRATCH"
}

expect_compile_error() {
  local name="$1" want="$2" out rc
  out="$(run_build)"; rc=$?
  if [[ $rc -eq 0 ]]; then report "$name" bad "expected a compile error, but the build SUCCEEDED"
  elif grep -qF "$want" <<<"$out"; then report "$name" ok
  else report "$name" bad "failed, but not with: $want"; fi
  restore
}

expect_success() {
  local name="$1" want="${2:-}" out rc
  out="$(run_build)"; rc=$?
  if [[ $rc -ne 0 ]]; then report "$name" bad "build FAILED: $(grep -m1 '^e: ' <<<"$out")"
  elif [[ -n "$want" ]] && ! grep -qF "$want" <<<"$out"; then
    report "$name" bad "built, but stdout lacked: $want"
  else report "$name" ok; fi
  restore
}

want() { [[ -z "$ONLY" || "$ONLY" == "$1" ]]; }

echo "Probing context-parameter claims (see template-logic/migration-status.md)"

# --- 1. context(x){} does NOT introduce an implicit receiver -------------
# githubUrl exists only on LibDetails, so it detects a receiver.
if want 1; then
  scratch <<'KT'
fun probe1(d: LibDetails): String = context(d) { githubUrl }
KT
  expect_compile_error "1. context(x){} does not leak a receiver" "Unresolved reference 'githubUrl'"
fi

# --- 2. CONTROL for 1: with(x){} DOES introduce a receiver --------------
# Without this, probe 1 could pass for the wrong reason (a typo'd name is also
# "unresolved"). This proves probe 1 actually discriminates.
if want 2; then
  scratch <<'KT'
fun probe2(d: LibDetails): String = with(d) { githubUrl }
KT
  expect_success "2. CONTROL: with(x){} does leak a receiver"
fi

# --- 3. context(..) takes several arguments, and _ propagates -----------
if want 3; then
  scratch <<'KT'
context(d: LibDetails, s: LibSettings) private fun inner(): String = "3:${d.name}:${s.withJvm}"
context(_: LibDetails, _: LibSettings) private fun conduit(): String = inner()
fun probe3(d: LibDetails): String = context(d, d.settings) { conduit() }
KT
  cat >>"$SCRIPT" <<'KT'
println("PROBE3 " + probe3(gradle.extLibDetails))
KT
  expect_success "3. context(a,b) works; context(_) forwards without naming" "PROBE3 3:KGround:"
fi

# --- 4. natural-syntax call from a FLAGLESS script is rejected ----------
# Receiverless on purpose, so "receiver type mismatch" cannot be the confound.
if want 4; then
  scratch <<'KT'
context(d: LibDetails) fun probe4(): String = d.name
KT
  cat >>"$SCRIPT" <<'KT'
println("PROBE4 " + probe4(gradle.extLibDetails))
KT
  expect_compile_error "4. flagless script cannot call a context fun naturally" \
    "To call contextual declarations, specify the '-Xcontext-parameters' compiler option."
fi

# --- 5. ...but the flattened-coercion escape hatch DOES work ------------
if want 5; then
  scratch <<'KT'
context(d: LibDetails) fun probe5(): String = "5:" + d.name
KT
  cat >>"$SCRIPT" <<'KT'
val probe5Ref: (pl.mareklangiewicz.deps.LibDetails) -> String = ::probe5
println("PROBE5 " + probe5Ref(gradle.extLibDetails))
KT
  expect_success "5. flattened coercion reaches a context fun, and runs" "PROBE5 5:KGround"
fi

# --- 6. a bare reference resolves but CANNOT be invoked -----------------
# This is why probe 5 needs the explicit type: the reference trick alone is
# not enough.
if want 6; then
  scratch <<'KT'
context(d: LibDetails) fun probe6(): String = d.name
KT
  cat >>"$SCRIPT" <<'KT'
val probe6Ref = ::probe6
println("PROBE6 " + probe6Ref())
KT
  expect_compile_error "6. bare reference cannot be invoked without the flag" \
    "To call contextual declarations, specify the '-Xcontext-parameters' compiler option."
fi

# --- residue check: probing must leave the tree exactly as it was -------
if ! git diff --quiet -- "$SCRIPT_REL" || [[ -e $SCRATCH ]]; then
  report "residue: tree unchanged after probing" bad "probes left the tree dirty; restoring via git"
  git checkout -- "$SCRIPT_REL"; rm -f "$SCRATCH"
else
  report "residue: tree unchanged after probing" ok
fi

echo
echo "  $pass passed, $fail failed"
[[ $fail -eq 0 ]]
