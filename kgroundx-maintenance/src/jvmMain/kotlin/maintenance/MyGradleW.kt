package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.*
import okio.FileSystem.Companion.RESOURCES
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ure.UReplacement


/**
 * My reproducers most of the time have nothing to do with gradle version, so it's nice to have gradle updated there,
 * but sometimes they depend on gradle version, so each update have to be checked/reproduced, before commiting/pushing.
 */
@ExampleApi suspend fun updateGradlewFilesInMyProjects(onlyPublic: Boolean, skipReproducers: Boolean) =
  getMyGradleProjectsPathS(onlyPublic).collect {
    val log = localULog()
    when {
      skipReproducers && it.segments.any { it == "reproducers" } -> log.i("Skipping reproducer $it")
      else -> updateGradlewFilesInProject(it)
    }
  }

@ExampleApi suspend fun updateGradlewFilesInKotlinProject(projectName: String) =
  updateGradlewFilesInProject(PathToKotlinProjects / projectName)

suspend fun updateGradlewFilesInProject(fullPath: Path) =
  gradlewRelPaths.forEach { gradlewRelPath ->
    val log = localULog()
    val fs = localUFileSys()
    val targetPath = fullPath / gradlewRelPath
    val oldContent = fs.readByteString(targetPath)
    val newContent = RESOURCES.readByteString("/templates".pth / gradlewRelPath.withName { "$it.tmpl" })
    if (oldContent == newContent) log.i("Skipping already updated gradlew file: $targetPath")
    else {
      val action = if (fs.exists(targetPath)) "Updating" else "Creating new"
      log.i("$action gradlew file: $targetPath")
      fs.writeByteString(targetPath, newContent)
    }
  }


@OptIn(DelicateApi::class)
private suspend fun findGradleRootProjectS(path: Path): Flow<Path> =
  findTypeRegex(path.strf, "f", ".*/settings.gradle\\(.kts\\)?")
    .reducedOutToFlow()
    .reducedMap {
      // $ at the end of regex is important to avoid matching generated resource like: settings.gradle.kts.tmpl
      val regex = Regex("/settings\\.gradle(\\.kts)?\$")
      map { regex.replaceSingle(it, UReplacement.Empty).pth }
    }
    .ax()

val gradlewRelPaths =
  listOf("", ".bat").map { "gradlew$it".pth } +
    listOf("jar", "properties").map { "gradle/wrapper/gradle-wrapper.$it".pth }

/** @return Full pathS of my gradle rootProjectS (dirs with settings.gradle[.kts] files) */
@OptIn(ExperimentalCoroutinesApi::class)
@ExampleApi private suspend fun getMyGradleProjectsPathS(onlyPublic: Boolean = true): Flow<Path> =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .flatMapConcat(::findGradleRootProjectS)


