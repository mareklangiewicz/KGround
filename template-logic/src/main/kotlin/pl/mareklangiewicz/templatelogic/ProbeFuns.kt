package pl.mareklangiewicz.templatelogic

import org.gradle.api.Project
import pl.mareklangiewicz.deps.*

// Experimental probe helpers for the context-parameter claims in migration-status.md.
// Branch-only; nothing in the build templates calls these.
//
// This file is compiled WITH -Xcontext-parameters. The build script that calls it is
// compiled WITHOUT. That is the real setup the claims are about, so no scaffolding is
// needed -- see the `probes` task in kgroundx-experiments/build.gradle.kts.

/**
 * Returns the PROJECT name.
 *
 * The whole reason entry points use `context(details)` and not `with(details)`:
 * LibDetails and Project both have `name`, so under `with` this would silently return
 * the LIBRARY name instead -- which is exactly how `coordinates(artifactId = name)` in
 * [defaultPublishing] would end up publishing every module under the wrong artifactId.
 * A context parameter is not a receiver, so it cannot shadow.
 */
context(details: LibDetails)
fun Project.probeNameIsProjectName(): String = name

context(d: LibDetails, s: LibSettings)
private fun probeInner(): String = "${d.name}/${s.withJvm}"

/** Holds both contexts only to forward them, so it names neither. */
context(_: LibDetails, _: LibSettings)
private fun probeConduit(): String = probeInner()

/** `context(a, b)` takes several arguments at once, and `_` still propagates downstream. */
fun probeMultiAndUnnamed(d: LibDetails): String = context(d, d.settings) { probeConduit() }

/** Target for the flattened-coercion probe: reachable from a flagless script. */
context(d: LibDetails)
fun probeContextFun(): String = "ctx:" + d.name
