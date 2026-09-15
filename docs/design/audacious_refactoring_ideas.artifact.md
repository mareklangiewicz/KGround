# Audacious Refactoring Ideas for template-logic

This document outlines ambitious strategies to transform `template-logic` into a world-class, type-safe, and highly declarative build system, leveraging the latest Kotlin and Gradle features.

## 1. Maximum Leverage of Context Parameters

Context parameters are the "killer feature" for build logic. We should aim to eliminate almost all explicit parameters from our template functions.

- **Global Contexts**: Define core context objects like `Project`, `LibDetails`, and `LibSettings`.
- **Nesting DSLs**: Create functions that require multiple contexts:
  ```kotlin
  context(Project, LibSettings, KotlinMultiplatformExtension)
  fun configureMppTargets() {
      if (withJvm) jvm()
      if (withAndro) androidTarget()
  }
  ```
- **Implicit Dependency Resolution**: Instead of passing `details` down the chain, logic can just "expect" a `LibDetails` to be in scope, allowing for extremely clean root scripts.

## 2. Purely Declarative Project "Personas"

Move away from calling configuration functions and toward defining project **intent**.

- **Persona DSL**: Instead of a "template", a project adopts a "persona".
  ```kotlin
  // build.gradle.kts
  id("my-convention")

  persona(MppLibrary) {
      features = [Compose, Android, Publishing]
  }
  ```
- **Constraint-Based Logic**: The `template-logic` evaluates the persona and its features, automatically resolving conflicts and applying the necessary plugins and configurations in the correct order.

## 3. Strong Typing with Value Classes

Prevent "Stringly-typed" build logic by wrapping identifiers in Kotlin `value classes`.

- **Domain Types**: Use `GroupId`, `ArtifactId`, `VersionCode`, and `Namespace` types.
- **VerSync 2.0**: The synchronization check can work on these types, ensuring that a `VersionCode` never accidentally gets assigned to a `MinSdk`.

## 4. Abstracting Plugin Differences (The "Build SDK")

Create a layer that abstracts away the specific APIs of AGP, KMP, and Compose.

- **Unified Dependencies**: A single DSL for adding dependencies that "knows" whether it's in a single-platform JVM project, an Android app, or an MPP module.
- **Target Independence**: Define a feature once (e.g., "this project uses SQLDelight") and let `template-logic` handle the distinct setup for JVM, Android, and Native.

## 5. Build-Time Integrity Linter

Integrate "Lint-as-Logic" directly into the `template-logic` module.

- **Structural Validation**: Automatically check if the project structure matches the `LibDetails` (e.g., check if the directory name matches the `ArtifactId`).
- **Dependency Guard**: Warn or fail if a module includes a dependency that contradicts its "Persona" (e.g., a "Pure MPP" module including an Android-only library).
- **Contract-Based Assertions**: Use Kotlin `contracts` to ensure that once a template is called, certain extensions are guaranteed to be initialized and non-null.

## 6. Functional Composition of Build Steps

Treat build configuration as a pipeline of transformations.

- **Composition over Inheritance**: Instead of huge `defaultBuildTemplate...` functions, use small, focused, context-aware functions that can be composed:
  ```kotlin
  project
    .withKotlin(2.4)
    .withStandardRepos()
    .withMppTargets()
    .withCompose()
    .withMavenCentralPublishing()
  ```

## 7. Intelligent Discovery

Use the power of the `template-logic` to "discover" what a project needs.

- **Source Set Detection**: Automatically enable the `withAndro` setting if a `src/androidMain` directory exists.
- **Metadata Auto-Generation**: Generate the `AndroidManifest.xml` or `proguard-rules.pro` dynamically based on the `LibDetails` provided in `settings.gradle.kts`.

---

> [!TIP]
> The goal is to make the `build.gradle.kts` files so simple they look like static configuration, while the `template-logic` provides a robust, compiled, and unit-tested engine that handles the complexity.
