// A module that exists only to hold ProbeFuns.kt, and only because of where it has to be compiled.
//
// The probes assert properties of the seam between build-logic sources and build SCRIPTS: what a
// .gradle.kts compiled WITHOUT -Xcontext-parameters can reach in a module compiled WITH it. That
// means the probed functions have to be on a build script's compile classpath, which means an
// included build. template-logic used to be that place; it is gone, and templatefun cannot be it --
// templatefun is published API, and these are branch-local evidence about an experiment.
//
// So: the smallest module that can host them. No plugin id, never published, seconds to build.
// The alternative was retiring the probes, which would delete the only executable evidence that a
// flagless build script can drive this API -- the exact property templatefun's public surface rests
// on. Keeping them costs this file.

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
  implementation("pl.mareklangiewicz.deps:DepsKt:0.4.29")
  // No longer needed for a symbol: AndroSdkCompileMinor collapsed into LibAndro in 0.4.29, so
  // probeSdkFull reads it off the andro scope from :deps instead. Kept deliberately, because
  // gate.sh's compile step is :probe-logic:compileKotlin and this is what makes that step exercise
  // the composite substitution for templatefun -- drop it and the gate stops checking the binding.
  implementation("pl.mareklangiewicz.deps:templatefun:0.4.29")
}

// The point of the module. Note the probes themselves report that module sources no longer NEED
// the flag (Kotlin 2.4.x enables context parameters by default at language version 2.4); the flag
// stays because the probes assert its effect, and -Xexplicit-context-arguments is a separate
// feature that is still opt-in.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
    freeCompilerArgs.add("-Xexplicit-context-arguments")
  }
}
