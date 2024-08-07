@file:Suppress("unused", "PackageDirectoryMismatch")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.flow.*
import okio.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.zenity.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ure.core.*


var PCodeKt = "/home/marek/code/kotlin".P

var PProjKGround = PCodeKt / "KGround"

var PProjRefreshDeps = PCodeKt / "refreshDeps"

var PProjDepsKt = PCodeKt / "DepsKt"



suspend fun Path.myKotlinFileDisable() = myKotlinFileToggleDisabled(true)
suspend fun Path.myKotlinFileEnable() = myKotlinFileToggleDisabled(false)

/** @param disable null will check if provided (existing) file looks like disabled and toggle it accordingly. */
@OptIn(DelicateApi::class)
suspend fun Path.myKotlinFileToggleDisabled(disable: Boolean? = null) {
  req(name.endsWith(".kt")) { "Doesn't look like kotlin (*.kt) file: $this" }
  val srcIdx = segments.indices.last { segments[it] == "src" }
  segments[srcIdx + 2].reqEq("kotlin") { "Unknown directory structure: $this" }
  testIfFileIsRegular(this).ax().reqTrue { "File (regular) not found: $this" }
  val looksDisabled = segments[srcIdx + 1].endsWith("Disabled")
  when (disable to looksDisabled) {
    true to true -> badArg { "Looks like already disabled." }
    false to false -> badArg { "Looks like NOT disabled." }
  }
  val newSegment = segments[srcIdx + 1].removeSuffix("Disabled") + if (looksDisabled) "" else "Disabled"
  val newP = rootOrPRel / segmentsBytes.take(srcIdx + 1) / newSegment / segmentsBytes.drop(srcIdx + 2)
  mkdir(newP.parent!!, withParents = true).ax()
  mvSingle(this, newP).ax()
  // TODO NOW findAndRmAllEmptyDirs(this.parent!!)
}


// TODO NOW implement it in KommandLine
//  (wrapper to sth like: find /path/to/dir -type d -empty -delete)
//  (also optional -mindepth? maxdepth?(too deep hierarchy is suspicious in most use-cases))
@DelicateApi
private fun findAndRmAllEmptyDirs(rootP: Path): Find = TODO()


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
        val ktFilePath = ktFilePathStr.P
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
  return map { PCodeKt / it }
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

