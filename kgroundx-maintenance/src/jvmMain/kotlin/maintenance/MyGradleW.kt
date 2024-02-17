package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.*
import okio.FileSystem.Companion.RESOURCES
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.regex.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.find.*


@ExampleApi suspend fun updateGradlewFilesInMyProjects(onlyPublic: Boolean, log: (Any?) -> Unit = ::println) =
    getMyGradleProjectsPathS(onlyPublic).collect {
        updateGradlewFilesInProject(it, log)
    }

@ExampleApi fun updateGradlewFilesInKotlinProject(projectName: String, log: (Any?) -> Unit = ::println) =
    updateGradlewFilesInProject(PathToMyKotlinProjects / projectName, log = log)

fun updateGradlewFilesInProject(fullPath: Path, log: (Any?) -> Unit = ::println) =
    gradlewRelPaths.forEach { gradlewRelPath ->
        val targetPath = fullPath / gradlewRelPath
        val content = RESOURCES.readByteString(gradlewRelPath.withName { "$it.tmpl" })
        val action = if (SYSTEM.exists(targetPath)) "Updating" else "Creating new"
        log("$action gradlew file: $targetPath")
        SYSTEM.writeByteString(targetPath, content)
    }


@OptIn(DelicateKommandApi::class)
private suspend fun findGradleRootProjectS(path: Path): Flow<Path> =
    findTypeRegex(path.toString(), "f", ".*/settings.gradle\\(.kts\\)?")
        .reducedOutToFlow()
        .reducedMap {
            // $ at the end of regex is important to avoid matching generated resource like: settings.gradle.kts.tmpl
            val regex = Regex("/settings\\.gradle(\\.kts)?\$")
            map { regex.replaceSingle(it, "").toPath() }
        }
        .exec(SYS)

val gradlewRelPaths =
    listOf("", ".bat").map { "gradlew$it".toPath() } +
            listOf("jar", "properties").map { "gradle/wrapper/gradle-wrapper.$it".toPath() }

/** @return Full pathS of my gradle rootProjectS (dirs with settings.gradle[.kts] files) */
@OptIn(ExperimentalCoroutinesApi::class)
@ExampleApi private suspend fun getMyGradleProjectsPathS(onlyPublic: Boolean = true): Flow<Path> =
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalKotlinProjectsPathS()
        .flatMapConcat(::findGradleRootProjectS)


