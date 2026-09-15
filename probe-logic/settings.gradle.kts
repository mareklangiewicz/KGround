rootProject.name = "probe-logic"

// Same switch as every other settings file here, and it has to be stated rather than inherited:
// this build is included by KGround's settings, not by its pluginManagement, so the parent's
// depsInclude does not reach it. Flip both together or probe-logic silently keeps resolving the
// published DepsKt while everything around it builds against the local one.
val depsDir = File(rootDir, "../../DepsKt").normalize()
val depsInclude =
  // depsDir.exists()
  false
if (depsInclude) {
  logger.warn("Including local build $depsDir")
  includeBuild(depsDir)
}
