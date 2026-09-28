package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.*
import okio.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ure.UReplacement


/**
 * All my gradle projects use ONE gradle version: the latest release, led by KGround.
 * KGround's own wrapper files (in its working tree) are the source every other project copies.
 *
 * 1. [updateGradlewInKGround] brings KGround's root wrapper to the latest release (if it isn't there yet).
 * 2. [updateGradlewFilesInMyProjects] copies KGround's root wrapper files to all my other gradle projects.
 *
 * BTW reproducers most of the time have nothing to do with gradle version, so it's nice to have gradle updated there,
 * but sometimes they depend on gradle version, so each update have to be checked/reproduced, before commiting/pushing.
 */
@ExampleApi suspend fun updateGradleEverywhere(dryRun: Boolean = false) {
  val kgroundChanges = updateGradlewInKGround(dryRun = dryRun)
  if (dryRun && kgroundChanges) localULog().w(
    "Dry run: KGround's wrapper was NOT updated, so below is compared with its CURRENT wrapper. " +
      "The real run updates KGround first, and then every project below differs from it.",
  )
  updateGradlewFilesInMyProjects(onlyPublic = false, skipReproducers = true, dryRun = dryRun)
}

data class GradleRelease(val version: String, val distributionSha256: String)

/** Parses https://services.gradle.org/versions/current (json; only the two fields we need). */
fun parseGradleRelease(versionsJson: String): GradleRelease {
  fun field(name: String) = Regex("\"$name\"\\s*:\\s*\"([^\"]*)\"").find(versionsJson)?.groupValues?.get(1)
    ?: bad { "No \"$name\" in gradle versions json" }
  return GradleRelease(field("version"), field("checksum")).also {
    it.distributionSha256.matches(Regex("[0-9a-f]{64}")).chkTrue { "Bad gradle distribution checksum: ${it.distributionSha256}" }
  }
}

@OptIn(DelicateApi::class)
suspend fun fetchGradleCurrentRelease(): GradleRelease =
  parseGradleRelease(kommand("curl", "-sSfL", "https://services.gradle.org/versions/current").ax().joinToString("\n"))

/** @return gradle version from distributionUrl in given gradle-wrapper.properties content, or null if not found. */
fun gradleVersionInWrapperProperties(content: String): String? =
  Regex("""distributionUrl=.*/gradle-([^/]+)-(bin|all)\.zip""").find(content)?.groupValues?.get(1)

private suspend fun Path.gradlewVersion(): String? {
  val fs = localUFileSys()
  val props = this / "gradle/wrapper/gradle-wrapper.properties"
  return if (fs.exists(props)) gradleVersionInWrapperProperties(fs.readUtf8(props)) else null
}

/**
 * Brings KGround's root wrapper to given [release] (latest by default).
 * @return true if KGround's wrapper was (or with [dryRun]: would be) changed
 * The gradle "wrapper" task writes the requested version to gradle-wrapper.properties, but the jar and the scripts
 * of the gradle version RUNNING it, so it has to run twice (the second run is already on the new version).
 * It runs in a tiny throwaway build, not in KGround itself: KGround doesn't configure the wrapper task,
 * so the output is the same, and it costs ~600M instead of configuring the whole KGround build.
 * Gradle only through gndx gradle (memory capped), see ~/AGENTS.md.
 */
@OptIn(DelicateApi::class)
suspend fun updateGradlewInKGround(release: GradleRelease? = null, dryRun: Boolean = false): Boolean {
  val log = localULog()
  val fs = localUFileSys()
  val rel = release ?: fetchGradleCurrentRelease()
  val old = PProjKGround.gradlewVersion()
  val props = fs.readUtf8(PProjKGround / "gradle/wrapper/gradle-wrapper.properties")
  if (old == rel.version && "distributionSha256Sum=${rel.distributionSha256}" in props) {
    log.i("KGround gradlew already at gradle ${rel.version}")
    return false
  }
  log.i("KGround gradlew: gradle $old -> ${rel.version}${if (dryRun) " (dry run)" else ""}")
  if (dryRun) return true
  val staging = fs.pathToSomeTmpOrHome / "kgroundx-gradlew-staging"
  if (fs.exists(staging)) fs.deleteRecursively(staging)
  fs.createDirectories(staging)
  fs.writeUtf8(staging / "settings.gradle.kts", "rootProject.name = \"kgroundx-gradlew-staging\"\n")
  updateGradlewFilesInProject(staging, quiet = true) // seed with the current wrapper, so it can run at all
  repeat(2) {
    kommand(
      "gndx", "gradle", "run", "wrapper", "--dir=${staging.strf}", "--no-stage", "--cap=1G", "--margin=256M",
      "--gradle-arg=--gradle-version=${rel.version}",
      "--gradle-arg=--gradle-distribution-sha256-sum=${rel.distributionSha256}",
    ).ax()
  }
  staging.gradlewVersion().chkEq(rel.version) { "Staging wrapper not at gradle ${rel.version}" }
  updateGradlewFilesInProject(PProjKGround, source = staging)
  fs.deleteRecursively(staging)
  return true
}

/**
 * Copies KGround's root wrapper files to all my gradle projects (all dirs with settings.gradle[.kts], so also nested
 * ones, like KGround's templates). Doesn't fetch or change any version: run [updateGradlewInKGround] first.
 */
@ExampleApi suspend fun updateGradlewFilesInMyProjects(onlyPublic: Boolean, skipReproducers: Boolean, dryRun: Boolean = false) {
  val log = localULog()
  log.i("Copying gradlew files (gradle ${PProjKGround.gradlewVersion()}) from $PProjKGround${if (dryRun) " (dry run)" else ""}")
  var updated = 0
  var upToDate = 0
  var skipped = 0
  getMyGradleProjectsPaths(onlyPublic).forEach {
    when {
      it == PProjKGround -> Unit
      skipReproducers && it.segments.any { it == "reproducers" } -> log.i("Skipping reproducer $it").also { skipped++ }
      updateGradlewFilesInProject(it, dryRun = dryRun) -> updated++
      else -> upToDate++
    }
  }
  log.i("Gradlew files: ${if (dryRun) "to update" else "updated"}: $updated, up to date: $upToDate, skipped: $skipped")
}

@ExampleApi suspend fun updateGradlewFilesInKotlinProject(projectName: String) =
  updateGradlewFilesInProject(PCodeKt / projectName)

/** @return true if anything was (or with [dryRun]: would be) changed */
@OptIn(DelicateApi::class)
suspend fun updateGradlewFilesInProject(
  fullPath: Path,
  source: Path = PProjKGround,
  dryRun: Boolean = false,
  quiet: Boolean = false,
): Boolean {
  val log = localULog()
  val fs = localUFileSys()
  val changed = gradlewRelPaths.filter { fs.readByteStringOrNull(fullPath / it) != fs.readByteString(source / it) }
  if (changed.isEmpty()) {
    if (!quiet) log.i("Up to date gradlew files (gradle ${fullPath.gradlewVersion()}): $fullPath")
    return false
  }
  if (!quiet) log.i("${if (dryRun) "Would update" else "Updating"} gradlew files " +
    "(gradle ${fullPath.gradlewVersion()} -> ${source.gradlewVersion()}): $fullPath ${changed.map { it.name }}")
  if (dryRun) return true
  for (rel in changed) {
    val target = fullPath / rel
    val isNew = !fs.exists(target)
    fs.createDirectories(target.parent!!)
    fs.writeByteString(target, fs.readByteString(source / rel))
    if (isNew && rel.name == "gradlew") kommand("chmod", "+x", target.strf).ax()
  }
  return true
}

private suspend fun UFileSys.readByteStringOrNull(path: Path) = if (exists(path)) readByteString(path) else null


@OptIn(DelicateApi::class)
private suspend fun findGradleRootProjects(path: Path): List<Path> =
  findTypeRegex(path, "f", ".*/settings.gradle\\(.kts\\)?")
    .reducedOutToList()
    .reducedMap {
      // $ at the end of regex is important to avoid matching generated resource like: settings.gradle.kts.tmpl
      val regex = Regex("/settings\\.gradle(\\.kts)?\$")
      map { regex.replaceSingle(it, UReplacement.Empty).P }
    }
    .ax()

val gradlewRelPaths =
  LO("", ".bat").map { "gradlew$it".P } +
    LO("jar", "properties").map { "gradle/wrapper/gradle-wrapper.$it".P }

/** @return Full paths of my gradle rootProjects (dirs with settings.gradle[.kts] files) */
@OptIn(ExperimentalCoroutinesApi::class)
@ExampleApi private suspend fun getMyGradleProjectsPaths(onlyPublic: Boolean = true): List<Path> =
  getMyProjectsNames(onlyPublic)
    .mapFilterLocalKotlinProjectsPaths()
    .flatMap { findGradleRootProjects(it) }
