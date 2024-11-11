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
  val srcP = rootOrPRel / segmentsBytes.take(srcIdx + 1)
  val srcChild = segments[srcIdx + 1] // this should be gradle "source set"
  segments[srcIdx + 2].reqEq("kotlin") { "Unknown directory structure: $this" }
  testIfFileIsRegular(this).ax().reqTrue { "File (regular) not found: $this" }
  val wasDisabled = srcChild.endsWith("Disabled")
  val srcChildNew: String = when (wasDisabled to (disable ?: !wasDisabled)) {
    true to true -> badArg { "Looks like already disabled." }
    false to false -> badArg { "Looks like NOT disabled." }
    false to true -> srcChild + "Disabled"
    else -> srcChild.removeSuffix("Disabled")
  }
  val fullPNew = srcP / srcChildNew / segmentsBytes.drop(srcIdx + 2)
  mkdir(fullPNew.parent!!, withParents = true).ax()
  mvSingle(this, fullPNew).ax()
  if (wasDisabled) // it was disabled, and we already moved the file away from ...src/sthDisabled
    findAndDeleteAllEmptyDirs(srcP / srcChild).ax() // clear up ...src/sthDisabled (only what's already empty).
}

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
  getMyProjectsNames(onlyPublic)
    .mapFilterLocalKotlinProjectsPaths(alsoFilter = alsoFilterProjectPath)
    .forEach { projectPath ->
      log.i("Searching in project: $projectPath")
      val listKt = findMyKotlinCode(projectPath).ax()
      val listKts =
        if (alsoGradleKts)
          findMyKotlinCode(
            projectPath,
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

/** @receiver Flow of projects names. */
@ExampleApi fun Flow<String>.mapFilterLocalKotlinProjectsPathS(
  alsoFilter: suspend (Path) -> Boolean = { true },
): Flow<Path> {
  return map { PCodeKt / it }
    .filter { localUFileSys().exists(it) }
    .filter { alsoFilter(it) }
}

/** @receiver Iterable of projects names. */
@ExampleApi suspend fun Iterable<String>.mapFilterLocalKotlinProjectsPaths(
  alsoFilter: suspend (Path) -> Boolean = { true },
): List<Path> {
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
  fetchMyProjectsNames(onlyPublic)
    .mapFilterLocalKotlinProjectsPaths()
    .forEach { path ->
      suspend fun inject() {
        log.i("Injecting my templates to project: $path")
        tryInjectMyTemplatesToProject(path, templates, askInteractively)
      }
      !askInteractively || zenityAskIf("Try to inject my templates to project: $path ?").ax() || return@forEach
      inject()
    }
}

@ExampleApi suspend fun fetchMyProjectsNameS(onlyPublic: Boolean = true): Flow<String> =
  ghMyRepoList(onlyPublic = onlyPublic)
    .outputFields("name")
    .reducedOutToFlow()
    .ax()


@ExampleApi suspend fun fetchMyProjectsNames(onlyPublic: Boolean = true, sorted: Boolean = true): List<String> =
  fetchMyProjectsNameS(onlyPublic).toList().let { if (sorted) it.sorted() else it }

@ExampleApi suspend fun getMyProjectsNames(onlyPublic: Boolean = true) =
  if (onlyPublic) getMyPublicProjectsNames() else getMyAllProjectsNames()

@ExampleApi suspend fun getMyPublicProjectsNames() =
  MyCachedPublicProjectsNames ?:
  fetchMyProjectsNames(onlyPublic = true)
    .also { MyCachedPublicProjectsNames = it }

@ExampleApi suspend fun getMyAllProjectsNames() =
  MyCachedAllProjectsNames ?:
  fetchMyProjectsNames(onlyPublic = false)
    .also { MyCachedAllProjectsNames = it }

@ExampleApi suspend fun getMyPrivateProjectsNames() = getMyAllProjectsNames() - getMyPublicProjectsNames()

@Volatile private var MyCachedPublicProjectsNames: List<String>? = null
@Volatile private var MyCachedAllProjectsNames: List<String>? = null

