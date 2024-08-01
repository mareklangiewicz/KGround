@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.flow.*
import okio.*
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kground.logEach
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.reducedOutToFlow
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.core.Ure


var PathToKotlinProjects = "/home/marek/code/kotlin".pth

var PathToKGroundProject = PathToKotlinProjects / "KGround"

var PathToRefreshDepsProject = PathToKotlinProjects / "refreshDeps"

var PathToDepsKtProject = PathToKotlinProjects / "DepsKt"

// TODO_later: refactor this little experiment fun
@OptIn(DelicateApi::class)
@ExampleApi suspend fun searchKotlinCodeInMyProjects(
  codeInLineUre: Ure,
  onlyPublic: Boolean = false,
  alsoGradleKts: Boolean = true,
  alsoFilterProjectPath: suspend (Path) -> Boolean = { true },
) {
  val log = localULog()
  var foundCount = 0
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS(alsoFilter = alsoFilterProjectPath)
    .collect { projectPath ->
      log.i("Searching in project: $projectPath")
      val listKt = findMyKotlinCode(projectPath.strf).ax()
      val listKts =
        if (alsoGradleKts)
          findMyKotlinCode(
            projectPath.strf,
            withNameBase = "*.gradle.kts",
            withNameFull = null,
          ).ax()
        else emptyList()
      (listKt + listKts).forEach { ktFilePathStr ->
        val ktFilePath = ktFilePathStr.pth
        val lineContentUre = codeInLineUre.withOptWhatevaAroundInLine()
        val result = readAndFindUreLineContentWithSomeLinesAround(ktFilePath, lineContentUre)
        result?.value?.let {
          foundCount++
          log.i("found in file ($foundCount): $ktFilePathStr")
          log.i("found code:")
          it.lines().logEach(log)
        }
      }
    }
  log.i("Total found files: $foundCount")
}


// TODO_someday: sth like this public in UreIO.kt
@DelicateApi("FIXME: Probably leads to catastrophic backtracking. Keep maxLinesAround < 3.")
private suspend fun readAndFindUreLineContentWithSomeLinesAround(
  file: Path,
  ureLineContent: Ure,
  maxLinesAround: Int = 1,
): MatchResult? {
  val log = localULog()
  val fs = localUFileSys()
  return fs.readUtf8(file).let { fileContent ->
    ureLineContent
      .withSomeLinesAround(log, maxLinesBefore = maxLinesAround, maxLinesAfter = maxLinesAround)
      .findFirstOrNull(fileContent)
  }
}

@DelicateApi("FIXME: Probably leads to catastrophic backtracking. Keep maxLinesBefore < 3.")
private fun Ure.withSomeLinesAround(
  log: ULog,
  maxLinesBefore: Int = 1,
  maxLinesAfter: Int = 1,
) = ure {
  if (maxLinesBefore > 2) log.w("FIXME: this is terribly slow for maxLinesBefore > 2")
  // FIXME investigate if it can be optimized. https://www.regular-expressions.info/catastrophic.html
  0..maxLinesBefore of ureAnyLine()
  +ureLineWithContent(this@withSomeLinesAround)
  0..maxLinesAfter of ureAnyLine()
}

@ExampleApi suspend fun checkMyDWorkflowsInMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { checkMyDWorkflowsInProject(it) }


@ExampleApi suspend fun injectMyDWorkflowsToMyProjects(onlyPublic: Boolean) =
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalDWorkflowsProjectsPathS()
    .collect { injectDWorkflowsToProject(it) }

@ExampleApi private fun Flow<String>.mapFilterLocalDWorkflowsProjectsPathS() =
  mapFilterLocalKotlinProjectsPathS {
    val log = localULog()
    val fs = localUFileSys()
    val isGradleRootProject = fs.exists(it / "settings.gradle.kts") || fs.exists(it / "settings.gradle")
    if (!isGradleRootProject) {
      log.w("Ignoring dworkflows in non-gradle project: $it")
    }
    // FIXME_maybe: Change when I have dworkflows for non-gradle projects
    isGradleRootProject
  }

/** @receiver Flow of projects names. */
@ExampleApi internal fun Flow<String>.mapFilterLocalKotlinProjectsPathS(
  alsoFilter: suspend (Path) -> Boolean = { true },
): Flow<Path> {
  return map { PathToKotlinProjects / it }
    .filter { localUFileSys().exists(it) }
    .filter { alsoFilter(it) }
}


@ExampleApi suspend fun tryToInjectMyTemplatesToAllMyProjects(
  onlyPublic: Boolean = false,
  askInteractively: Boolean = true,
) {
  val log = localULog()
  val templates = collectMyTemplates()
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .collect { path ->
      suspend fun inject() {
        log.i("Injecting my templates to project: $path")
        tryInjectMyTemplatesToProject(path, templates, askInteractively)
      }
      !askInteractively || zenityAskIf("Try to inject my templates to project: $path ?").ax() || return@collect
      inject()
    }
}

@Deprecated("Temporary fun to fix templates from single to double square brackets marks")
@ExampleApi suspend fun tryFixMyTemplatesInAllMyProjects(
  onlyPublic: Boolean = false,
  askInteractively: Boolean = true,
) {
  val log = localULog()
  fetchMyProjectsNameS(onlyPublic)
    .mapFilterLocalKotlinProjectsPathS()
    .collect { path ->
      suspend fun fix() {
        log.i("Fixing my templates in project: $path")
        tryFixMyTemplatesInProject(path, askInteractively)
      }
      !askInteractively || zenityAskIf("Try to fix my templates in project: $path ?").ax() || return@collect
      fix()
    }
}

@Suppress("IdentifierGrammar")
@ExampleApi suspend fun fetchMyProjectsNameS(onlyPublic: Boolean = true): Flow<String> =
  ghMyRepoList(onlyPublic = onlyPublic)
    .outputFields("name")
    .reducedOutToFlow()
    .ax()


@ExampleApi suspend fun fetchMyProjectsNames(onlyPublic: Boolean = true, sorted: Boolean = true): List<String> =
  fetchMyProjectsNameS(onlyPublic).toList().let { if (sorted) it.sorted() else it }

