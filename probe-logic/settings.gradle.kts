rootProject.name = "probe-logic"

// Local DepsKt when it is next door, exactly like every other settings file here. This build is
// part of the composite, but it is included by KGround's settings, not by pluginManagement, so it
// states its own switch rather than inheriting one.
val depsDir = File(rootDir, "../../DepsKt").normalize()
if (depsDir.exists()) {
  logger.warn("Including local build $depsDir")
  includeBuild(depsDir)
}
