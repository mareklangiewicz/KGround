// Empty on purpose. Applying it is the only way to get probe-logic's classes onto a build script's
// compile classpath -- includeBuild alone does not do that; it only makes the build's plugins and
// published coordinates resolvable. See probe-logic/build.gradle.kts for why this module exists.
