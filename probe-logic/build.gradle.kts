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
  implementation("pl.mareklangiewicz.deps:DepsKt:0.4.28")
  // Only for AndroSdkCompileMinor, which probeSdkFull asserts against. Naming the same const the
  // templates use is the point: a probe that inlined the number would keep passing after the
  // templates moved on. The composite substitutes both of these to the local DepsKt build.
  implementation("pl.mareklangiewicz.deps:templatefun:0.4.28")
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
