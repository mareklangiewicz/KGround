package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.*
import org.gradle.api.publish.maven.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Kotlin Module Build Template]]

// Kind of experimental/temporary.. not sure how it will evolve yet,
// but currently I need these kind of substitutions/locals often enough
// especially when updating kground <-> kommandline (trans deps issues)
fun Project.setMyWeirdSubstitutions(
  vararg rules: Pair<String, String>,
  myProjectsGroup: String = "pl.mareklangiewicz",
  tryToUseLocalProjects: Boolean = true,
) {
  val foundLocalProjects: Map<String, Project?> =
    if (tryToUseLocalProjects) rules.associate { it.first to findProject(":${it.first}") }
    else emptyMap()
  configurations.all {
    resolutionStrategy.dependencySubstitution {
      for ((projName, projVer) in rules)
        substitute(module("$myProjectsGroup:$projName"))
          .using(
            // Note: there are different fun in gradle: Project.project; DependencySubstitution.project
            if (foundLocalProjects[projName] != null) project(":$projName")
            else module("$myProjectsGroup:$projName:$projVer")
          )
    }
  }
}

/**
 * MIGRATED to the sibling model. Was `context(settings: LibSettings)` + `with(settings.repos)` —
 * the design note's own example of "helpers reach through the tree". As a sibling there is nothing
 * to reach through: the repo settings arrive directly.
 *
 * The context parameter is `reposSettings`, NOT `repos`, on purpose. This body calls
 * `maven(repos.kotlinx)`, where `repos` is a top-level DepsKt object; a context parameter named
 * `repos` would take that name and break those calls. Worth remembering alongside probe 1 — a
 * context parameter does not shadow an extension RECEIVER, but it does occupy its own name.
 */
context(reposSettings: LibReposSettingsTMP)
fun RepositoryHandler.addRepos() = with(reposSettings) {
  @Suppress("DEPRECATION")
  if (withMavenLocal) mavenLocal()
  if (withMavenCentral) mavenCentral()
  if (withGradle) gradlePluginPortal()
  if (withGoogle) google()
  if (withKotlinx) maven(repos.kotlinx)
  if (withKotlinxHtml) maven(repos.kotlinxHtml)
  if (withComposeJbDev) maven(repos.composeJbDev)
  if (withKtorEap) maven(repos.ktorEap)
  if (withJitpack) maven(repos.jitpack)
}

// TODO_maybe: doc says it could be now also applied globally instead for each task (and it works for andro too)
//   But it's only for jvm+andro, so probably this is better:
//   https://kotlinlang.org/docs/gradle-compiler-options.html#for-all-kotlin-compilation-tasks
fun TaskCollection<Task>.defaultKotlinCompileOptions(
  apiVer: KotlinVersion = KotlinVersion.KOTLIN_2_1,
  jvmTargetVer: String? = null, // it's better to use jvmToolchain (normally done in fun allDefault)
  renderInternalDiagnosticNames: Boolean = false,
) = withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions {
    apiVersion.set(apiVer)
    jvmTargetVer?.let { jvmTarget = JvmTarget.fromTarget(it) }
    if (renderInternalDiagnosticNames) freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
    // useful, for example, to suppress some errors when accessing internal code from some library, like:
    // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
  }
}

fun KotlinMultiplatformExtension.defaultCompiler(
  kotlinVer: KotlinVersion = KotlinVersion.KOTLIN_2_3,
  jvmVer: Int? = null,
  renderInternalDiagnosticNames: Boolean = false,
) {
  compilerOptions {
    languageVersion.set(kotlinVer)
    apiVersion.set(kotlinVer)
    if (renderInternalDiagnosticNames) freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
    freeCompilerArgs.add("-Xcontext-parameters")
    // useful, for example, to suppress some errors when accessing internal code from some library, like:
    // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
  }
  jvmVer?.let(::jvmToolchain)
}

fun TaskCollection<Task>.defaultTestsOptions(
  printStandardStreams: Boolean = true,
  printStackTraces: Boolean = true,
  onJvmUseJUnitPlatform: Boolean = true,
) = withType<AbstractTestTask>().configureEach {
  testLogging {
    showStandardStreams = printStandardStreams
    showStackTraces = printStackTraces
  }
  if (onJvmUseJUnitPlatform) (this as? Test)?.useJUnitPlatform()
}

// Provide artifacts information required by Maven Central
context(details: LibDetailsTMP)
fun MavenPom.defaultPOM() {
  name put details.name
  description put details.description
  url put details.githubUrl

  licenses {
    license {
      name put details.licenceName
      url put details.licenceUrl
    }
  }
  developers {
    developer {
      id put details.authorId
      name put details.authorName
      email put details.authorEmail
    }
  }
  scm { url put details.githubUrl }
}

/**
 * MIGRATED to the sibling model. The nested version reached through ONE field
 * (`details.settings.withCentralPublish`) for a single flag; as siblings that flag arrives as its
 * own context parameter, so this function names exactly the two things it uses and nothing else.
 *
 * Note it is still `context(..)` and not `with(..)`: [LibDetailsTMP] has a `name` too, and
 * `coordinates(artifactId = name)` must resolve to the PROJECT name. See [probeNameIsProjectName].
 */
context(details: LibDetailsTMP, settings: LibSettingsTMP)
fun Project.defaultPublishing() = extensions.configure<MavenPublishBaseExtension> {
  propertiesTryOverride("signingInMemoryKey", "signingInMemoryKeyPassword", "mavenCentralPassword")
  if (settings.withCentralPublish) publishToMavenCentral(automaticRelease = false)
  signAllPublications()
  signAllPublicationsFixSignatoryIfFound()
  // Note: artifactId is not details.name but current project.name (module name)
  coordinates(groupId = details.group, artifactId = name, version = details.version.str)
  pom { defaultPOM() }
}

/**
 * Sibling-model replacement for DepsKt's `defaultGroupAndVerAndDescription(lib: LibDetails)`.
 *
 * That one is published and takes the nested type, so the prototype cannot migrate it in place; it
 * is restated here over [LibDetailsTMP]. It reads only identity fields, which is the point: it never
 * needed `settings` at all, and as a sibling it cannot even see it.
 */
context(details: LibDetailsTMP)
fun Project.defaultGroupAndVerAndDescriptionTMP() {
  group = details.group
  version = details.version.str
  description = details.description
}

// endregion [[Kotlin Module Build Template]]
