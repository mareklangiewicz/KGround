@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.flow.*
import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.ure.*


// TODO_later: refactor this little experiment fun
@OptIn(DelicateKommandApi::class, DelicateApi::class)
internal suspend fun searchKotlinCodeInMyProjects(
    codeInLineUre: Ure,
    onlyPublic: Boolean = false,
    alsoFilterProjectPath: suspend FileSystem.(Path) -> Boolean = { true },
    log: (Any?) -> Unit = ::println,
) {
    var foundCount = 0
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalKotlinProjectsPathS(alsoFilter = alsoFilterProjectPath)
        .collect { projectPath ->
            log("Searching in project: $projectPath")
            findMyKotlinCode(projectPath.toString()).exec(SYS).forEach { ktFilePathStr ->
                val ktFilePath = ktFilePathStr.toPath()
                val lineContentUre = codeInLineUre.withOptWhatevaAroundInLine()
                val result = SYSTEM.readAndFindUreLineContentWithSomeLinesAround(ktFilePath, lineContentUre)
                result?.value?.let {
                    foundCount ++
                    log("found in file ($foundCount): $ktFilePathStr")
                    log("found code:")
                    log(it)
                }
            }
        }
    log("Total found files: $foundCount")
}


// TODO_someday: sth like this public in UreIO.kt
@DelicateApi("FIXME: Probably leads to catastrophic backtracking. Keep maxLinesAround < 3.")
private fun FileSystem.readAndFindUreLineContentWithSomeLinesAround(
    file: Path,
    ureLineContent: Ure,
    maxLinesAround: Int = 1,
): MatchResult? = readUtf8(file).let { fileContent ->
    ureLineContent.withSomeLinesAround(
        maxLinesBefore = maxLinesAround,
        maxLinesAfter = maxLinesAround
    ).findFirstOrNull(fileContent)
}

@DelicateApi("FIXME: Probably leads to catastrophic backtracking. Keep maxLinesBefore < 3.")
private fun Ure.withSomeLinesAround(
    maxLinesBefore: Int = 1,
    maxLinesAfter: Int = 1,
) = ure {
    if (maxLinesBefore > 2) println("FIXME: this is terribly slow for maxLinesBefore > 2")
        // FIXME investigate if it can be optimized. https://www.regular-expressions.info/catastrophic.html
    0..maxLinesBefore of ureAnyLine()
    + ureLineWithContent(this@withSomeLinesAround)
    0..maxLinesAfter of ureAnyLine()
}

suspend fun checkMyDWorkflowsInMyProjects(onlyPublic: Boolean, log: (Any?) -> Unit = ::println) =
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalDWorkflowsProjectsPathS(log = log)
        .collect { SYSTEM.checkMyDWorkflowsInProject(it, verbose = true, log = log) }


suspend fun injectMyDWorkflowsToMyProjects(onlyPublic: Boolean, log: (Any?) -> Unit = ::println) =
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalDWorkflowsProjectsPathS(log = log)
        .collect { SYSTEM.injectDWorkflowsToProject(it, log = log) }

private fun Flow<String>.mapFilterLocalDWorkflowsProjectsPathS(
    localSystem: FileSystem = SYSTEM,
    log: (Any?) -> Unit = ::println,
) = mapFilterLocalKotlinProjectsPathS(localSystem) {
    val isGradleRootProject = exists(it / "settings.gradle.kts") || exists(it / "settings.gradle")
    if (!isGradleRootProject) log("Ignoring dworkflows in non-gradle project: $it")
    // FIXME_maybe: Change when I have dworkflows for non-gradle projects
    isGradleRootProject
}

/** @receiver Flow of projects names. */
internal fun Flow<String>.mapFilterLocalKotlinProjectsPathS(
    localSystem: FileSystem = SYSTEM,
    alsoFilter: suspend FileSystem.(Path) -> Boolean = { true }
) = map { PathToMyKotlinProjects / it }
    .filter { localSystem.exists(it) }
    .filter { localSystem.alsoFilter(it) }



@NotPortableApi
suspend fun checkAllKnownRegionsInMyProjects(onlyPublic: Boolean = false, log: (Any?) -> Unit = ::println) =
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalKotlinProjectsPathS()
        .collect {
            log("Check all known regions in project: $it")
            SYSTEM.checkAllKnownRegionsInAllFoundFiles(it, verbose = true, log = log)
        }

@NotPortableApi
suspend fun injectAllKnownRegionsToMyProjects(onlyPublic: Boolean = false, log: (Any?) -> Unit = ::println) =
    fetchMyProjectsNameS(onlyPublic)
        .mapFilterLocalKotlinProjectsPathS()
        .collect {
            log("Inject all known regions to project: $it")
            SYSTEM.injectAllKnownRegionsToAllFoundFiles(it, log = log)
        }

val PathToMyKotlinProjects = "/home/marek/code/kotlin".toPath()

@Suppress("IdentifierGrammar")
suspend fun fetchMyProjectsNameS(onlyPublic: Boolean = true): Flow<String> =
    ghMarekLangiewiczRepoList(onlyPublic = onlyPublic)
        .outputFields("name")
        .reducedOutToFlow()
        .exec(SYS)


suspend fun fetchMyProjectsNames(onlyPublic: Boolean = true, sorted: Boolean = true): List<String> =
    fetchMyProjectsNameS(onlyPublic).toList().let { if (sorted) it.sorted() else it }

