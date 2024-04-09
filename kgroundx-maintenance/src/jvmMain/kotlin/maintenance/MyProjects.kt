@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.flow.*
import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.reducedOutToFlow
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.core.Ure


// TODO_later: refactor this little experiment fun
@OptIn(DelicateApi::class)
@ExampleApi suspend fun searchKotlinCodeInMyProjects(
  codeInLineUre: Ure,
  onlyPublic: Boolean = false,
  alsoFilterProjectPath: suspend FileSystem.(Path) -> Boolean = { true },
) {
  var foundCount = 0
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS(alsoFilter = alsoFilterProjectPath)
    .collect { projectPath ->
      ulog.i("Searching in project: $projectPath")
      findMyKotlinCode(projectPath.toString()).ax(SYS).forEach { ktFilePathStr ->
        val ktFilePath = ktFilePathStr.toPath()
        val lineContentUre = codeInLineUre.withOptWhatevaAroundInLine()
        val result = SYSTEM.readAndFindUreLineContentWithSomeLinesAround(ktFilePath, lineContentUre)
        result?.value?.let {
          foundCount++
          ulog.i("found in file ($foundCount): $ktFilePathStr")
          ulog.i("found code:\n$it")
        }
      }
    }
  ulog.i("Total found files: $foundCount")
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
      maxLinesAfter = maxLinesAround,
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
  +ureLineWithContent(this@withSomeLinesAround)
  0..maxLinesAfter of ureAnyLine()
}

@ExampleApi suspend fun checkMyDWorkflowsInMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { SYSTEM.checkMyDWorkflowsInProject(it) }


@ExampleApi suspend fun injectMyDWorkflowsToMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { SYSTEM.injectDWorkflowsToProject(it) }

@ExampleApi private fun Flow<String>.mapFilterLocalDWorkflowsProjectsPathS(
  localSystem: FileSystem = SYSTEM,
) = mapFilterLocalKotlinProjectsPathS(localSystem) {
  val isGradleRootProject = exists(it / "settings.gradle.kts") || exists(it / "settings.gradle")
  if (!isGradleRootProject) ulog.w("Ignoring dworkflows in non-gradle project: $it")
  // FIXME_maybe: Change when I have dworkflows for non-gradle projects
  isGradleRootProject
}

/** @receiver Flow of projects names. */
@ExampleApi internal fun Flow<String>.mapFilterLocalKotlinProjectsPathS(
  localSystem: FileSystem = SYSTEM,
  alsoFilter: suspend FileSystem.(Path) -> Boolean = { true },
) = map { PathToMyKotlinProjects / it }
  .filter { localSystem.exists(it) }
  .filter { localSystem.alsoFilter(it) }


@ExampleApi suspend fun checkAllKnownRegionsInMyProjects(onlyPublic: Boolean = false) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .collect {
      ulog.i("Check all known regions in project: $it")
      SYSTEM.checkAllKnownRegionsInAllFoundFiles(it)
    }

@ExampleApi suspend fun injectAllKnownRegionsToMyProjects(onlyPublic: Boolean = false) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .collect {
      ulog.i("Inject all known regions to project: $it")
      SYSTEM.injectAllKnownRegionsToAllFoundFiles(it)
    }

@ExampleApi val PathToMyKotlinProjects = "/home/marek/code/kotlin".toPath()

@Suppress("IdentifierGrammar")
@ExampleApi suspend fun fetchMyProjectsNameS(onlyPublic: Boolean = true): Flow<String> =
  ghMyRepoList(onlyPublic = onlyPublic)
    .outputFields("name")
    .reducedOutToFlow()
    .ax(SYS)


@ExampleApi suspend fun fetchMyProjectsNames(onlyPublic: Boolean = true, sorted: Boolean = true): List<String> =
  fetchMyProjectsNameS(onlyPublic).toList().let { if (sorted) it.sorted() else it }

