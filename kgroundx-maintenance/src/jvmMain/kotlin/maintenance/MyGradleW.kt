package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.*
import okio.FileSystem.Companion.RESOURCES
import okio.Path.Companion.toPath
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.ure.UReplacement


@ExampleApi suspend fun updateGradlewFilesInMyProjects(onlyPublic: Boolean) =
  getMyGradleProjectsPathS(onlyPublic).collect {
    updateGradlewFilesInProject(it)
  }

@ExampleApi suspend fun updateGradlewFilesInKotlinProject(projectName: String) =
  updateGradlewFilesInProject(PathToKotlinProjects / projectName)

suspend fun updateGradlewFilesInProject(fullPath: Path) =
  gradlewRelPaths.forEach { gradlewRelPath ->
    val log = implictx<ULog>()
    val fs = implictx<UFileSys>()
    val targetPath = fullPath / gradlewRelPath
    val content = RESOURCES.readByteString("/templates".toPath() / gradlewRelPath.withName { "$it.tmpl" })
    val action = if (fs.exists(targetPath)) "Updating" else "Creating new"
    log.i("$action gradlew file: $targetPath")
    fs.writeByteString(targetPath, content)
  }


@OptIn(DelicateApi::class)
private suspend fun findGradleRootProjectS(path: Path): Flow<Path> =
  findTypeRegex(path.toString(), "f", ".*/settings.gradle\\(.kts\\)?")
    .reducedOutToFlow()
    .reducedMap {
      // $ at the end of regex is important to avoid matching generated resource like: settings.gradle.kts.tmpl
      val regex = Regex("/settings\\.gradle(\\.kts)?\$")
      map { regex.replaceSingle(it, UReplacement.Empty).toPath() }
    }
    .ax()

val gradlewRelPaths =
  listOf("", ".bat").map { "gradlew$it".toPath() } +
    listOf("jar", "properties").map { "gradle/wrapper/gradle-wrapper.$it".toPath() }

/** @return Full pathS of my gradle rootProjectS (dirs with settings.gradle[.kts] files) */
@OptIn(ExperimentalCoroutinesApi::class)
@ExampleApi private suspend fun getMyGradleProjectsPathS(onlyPublic: Boolean = true): Flow<Path> =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .flatMapConcat(::findGradleRootProjectS)


