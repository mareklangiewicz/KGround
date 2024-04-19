package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import okio.FileSystem.Companion.RESOURCES
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.ure.*
import kotlin.math.*
import kotlin.random.*
import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.ulog.e
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.w

// FIXME NOW: I should not need to hardcode all these labels. Dynamically collect all special regions instead.
const val labelRoot = "Root Build Template"
const val labelKotlinModule = "Kotlin Module Build Template"
const val labelMppModule = "MPP Module Build Template"
const val labelMppApp = "MPP App Build Template"
const val labelComposeMppModule = "Compose MPP Module Build Template"
const val labelComposeMppApp = "Compose MPP App Build Template"
const val labelAndroCommon = "Andro Common Build Template"
const val labelAndroLib = "Andro Lib Build Template"
const val labelAndroApp = "Andro App Build Template"
const val labelFullMppLib = "Full MPP Lib Build Template"
const val labelFullMppApp = "Full MPP App Build Template"

// paths to templates dirs with build files, relative to MyKGroundRootPath
private const val pathMppRoot = "template-mpp"
private const val pathMppLib = "template-mpp/template-mpp-lib"
private const val pathMppApp = "template-mpp/template-mpp-app"
private const val pathAndroRoot = "template-andro"
private const val pathAndroLib = "template-andro/template-andro-lib"
private const val pathAndroApp = "template-andro/template-andro-app"

private data class RegionInfo(val label: String, val path: Path, val syncedPaths: List<Path>)

private val RegionInfo.pathInRes get() = "templates".toPath() / path / "build.gradle.kts.tmpl"
// TODO_maybe: special regions in settings.gradle.kts??
// pathInRes has to have different suffix from "build.gradle.kts" otherwise gradle sometimes tries to run it…
// (even just .kts extension sometimes confuses at least IDE)

// FIXME NOW separate it from Ure and move Ure common code to better places (later to separate lib)

private val RegionInfo.pathInSrc get() = MyKGroundRootPath / path / "build.gradle.kts"

private val RegionInfo.syncedPathsArrInSrc
  get() = syncedPaths.map { MyKGroundRootPath / it / "build.gradle.kts" }.toTypedArray()

private fun info(label: String, dir: String, vararg syncedDirs: String) =
  RegionInfo(label, dir.toPath(), syncedDirs.toList().map { it.toPath() })

private val regionsInfos = listOf(
  info(labelRoot, pathMppRoot, pathAndroRoot),
  info(labelKotlinModule, pathMppLib, pathMppApp, pathAndroLib, pathAndroApp),
  info(labelMppModule, pathMppLib, pathMppApp),
  info(labelMppApp, pathMppApp),
  info(labelComposeMppModule, pathMppLib, pathMppApp),
  info(labelComposeMppApp, pathMppApp),
  // update: notice that even for andro stuff, the source of truth is now mpp template
  info(labelAndroCommon, pathMppLib, pathMppApp, pathAndroLib, pathAndroApp),
  info(labelAndroLib, pathMppLib, pathAndroLib),
  info(labelAndroApp, pathMppApp, pathAndroApp),
  info(labelFullMppLib, pathMppLib),
  info(labelFullMppApp, pathMppApp),
)

private operator fun List<RegionInfo>.get(label: String) =
  find { it.label == label } ?: bad { "Unknown region label: $label" }

@OptIn(NotPortableApi::class) // it's jvmMain anyway
private fun knownRegion(regionLabel: String): String {
  val inputResPath = regionsInfos[regionLabel].pathInRes
  val ureWithRegion = ureWithSpecialRegion(regionLabel)
  val mr = RESOURCES.readAndMatchUre(inputResPath, ureWithRegion) ?: bad { "No region [$regionLabel] in $inputResPath" }
  return mr["region"]
}

private fun knownRegionFullTemplatePath(regionLabel: String) =
  SYSTEM.canonicalize(regionsInfos[regionLabel].pathInSrc)

fun checkAllKnownRegionsInProject(projectPath: Path) = try {
  ulog.i("BEGIN: Check all known regions in project:")
  SYSTEM.checkAllKnownRegionsInAllFoundFiles(projectPath)
  ulog.i("END: Check all known regions in project.")
} catch (e: IllegalStateException) {
  ulog.e("ERROR: ${e.message}")
}

fun injectAllKnownRegionsInProject(projectPath: Path) {
  ulog.i("BEGIN: Inject all known regions in project:")
  SYSTEM.injectAllKnownRegionsToAllFoundFiles(projectPath)
  ulog.i("END: Inject all known regions in project.")
}

// This actually is self-check for templates in KGround, so it should be in some integration test.
@ExampleApi fun checkAllKnownRegionsSynced() =
  regionsInfos.forEach {
    SYSTEM.checkKnownRegion(it.label, it.pathInSrc, *it.syncedPathsArrInSrc)
  }

@ExampleApi fun injectAllKnownRegionsToSync() =
  regionsInfos.forEach {
    SYSTEM.injectKnownRegion(it.label, *it.syncedPathsArrInSrc, addIfNotFound = false)
  }

fun FileSystem.checkAllKnownRegionsInAllFoundFiles(
  outputTreePath: Path,
  outputFileExt: String = "gradle.kts",
  failIfNotFound: Boolean = false,
) {
  val outputPaths = findAllFiles(outputTreePath).filterExt(outputFileExt).toList().toTypedArray()
  for (label in regionsInfos.map { it.label })
    checkKnownRegion(label, *outputPaths, failIfNotFound = failIfNotFound)
}

fun FileSystem.checkKnownRegionInAllFoundFiles(
  regionLabel: String,
  outputTreePath: Path,
  outputFileExt: String = "gradle.kts",
  failIfNotFound: Boolean = false,
) {
  val outputPaths = findAllFiles(outputTreePath).filterExt(outputFileExt).toList().toTypedArray()
  checkKnownRegion(regionLabel, *outputPaths, failIfNotFound = failIfNotFound)
}

fun FileSystem.injectAllKnownRegionsToAllFoundFiles(
  outputTreePath: Path,
  outputFileExt: String = "gradle.kts",
  addIfNotFound: Boolean = false,
) {
  val outputPaths = findAllFiles(outputTreePath).filterExt(outputFileExt).toList().toTypedArray()
  for (label in regionsInfos.map { it.label })
    injectKnownRegion(label, *outputPaths, addIfNotFound = addIfNotFound)
}

fun FileSystem.injectKnownRegionToAllFoundFiles(
  regionLabel: String,
  outputTreePath: Path,
  outputFileExt: String = "gradle.kts",
  addIfNotFound: Boolean = false,
) {
  val outputPaths = findAllFiles(outputTreePath).filterExt(outputFileExt).toList().toTypedArray()
  injectKnownRegion(regionLabel, *outputPaths, addIfNotFound = addIfNotFound)
}

@OptIn(DelicateApi::class)
fun FileSystem.checkKnownRegion(
  regionLabel: String,
  vararg outputPaths: Path,
  failIfNotFound: Boolean = true,
  interactive: Boolean = isInteractiveCodeEnabled(),
) = outputPaths.forEach { outputPath ->
  val canonPath1 = knownRegionFullTemplatePath(regionLabel).toString()
  val canonPath2 = canonicalize(outputPath).toString()
  val hint = "Maybe try running in CLI sth like:\ndiff $canonPath1 $canonPath2"
  try {
    checkCustomRegion(
      regionLabel,
      knownRegion(regionLabel),
      outputPath,
      failIfNotFound,
      hint,
    )
  }
  catch (e: NotEqStateErr) {
    interactive || throw e
    runBlocking { // FIXME_someday: all this check... functions (and other) should be correctly io suspendable.
      val msgDiff = "ideDiff(\n  \"$canonPath1\",\n  \"$canonPath2\"\n)"
      val question = "Incorrect region [$regionLabel] detected.\nTry opening diff in IDE?\n$msgDiff"
      val answer = zenityAskIf(question).ax()
      if (answer) ideDiff(canonPath1, canonPath2).ax()
      zenityAskIf("Continue checking? (No -> abort/fail/rethrow)").ax() || throw e
    }
  }
}

@OptIn(NotPortableApi::class) // it's jvmMain anyway
private fun FileSystem.checkCustomRegion(
  regionLabel: String,
  regionExpected: String,
  outputPath: Path,
  failIfNotFound: Boolean = true,
  checkFailedHint: String? = null,
) {
  val ureWithRegion = ureWithSpecialRegion(regionLabel)
  ureWithRegion.compile().matches(regionExpected).reqTrue { "regionExpected doesn't match region [$regionLabel]" }
  val region by readAndMatchUre(outputPath, ureWithRegion)
    ?: if (failIfNotFound) bad { "Region [$regionLabel] not found in $outputPath" } else return
  region.trimEnd('\n').chkEq(regionExpected.trimEnd('\n')) {
      ulog.w("Region: [$regionLabel] in File: $outputPath was modified.")
      checkFailedHint?.let { ulog.w(it) }
    "Region: [$regionLabel] in File: $outputPath was modified."
  }
  ulog.i("OK [$regionLabel] in $outputPath")
}

fun FileSystem.injectKnownRegion(
  regionLabel: String,
  vararg outputPaths: Path,
  addIfNotFound: Boolean = true,
) = injectCustomRegion(regionLabel, knownRegion(regionLabel), *outputPaths, addIfNotFound = addIfNotFound)

@OptIn(NotPortableApi::class)
fun FileSystem.injectCustomRegion(
  regionLabel: String,
  region: String,
  vararg outputPaths: Path,
  addIfNotFound: Boolean = true,
) = outputPaths.forEach { outputPath ->
  val regex = ureWithSpecialRegion(regionLabel).compile()
  processFile(outputPath, outputPath) { output ->
    val outputMR = regex.matchEntire(output)
    if (outputMR == null) {
      ulog.i("Inject [$regionLabel] to $outputPath - No match.")
      if (addIfNotFound) {
        ulog.i("Adding new region at the end.")
        output + "\n\n" + region.trimEnd()
      } else null
    } else {
      val before by outputMR
      val after by outputMR
      val newAfter = if (after.isNotEmpty() && region.last() != '\n') "\n" + after else after

      // val newRegion = if (newAfter.isEmpty()) region.trimEnd() else region
      val newRegion = region
      // FIXME_later: not sure why region was trimmed when at the end,
      // but I now have .editorconfig:insert_final_new_line, so I don't want that.
      // later analyze it more and refactor to clean and correct implementation of these injects

      val newOutput = before + newRegion + newAfter
      val summary =
        if (newOutput == output) "No changes." else "Changes detected (len ${output.length}->${newOutput.length})"
      ulog.i("Inject [$regionLabel] to $outputPath - $summary")
      newOutput
    }
  }
}

fun downloadTmpFile(
  url: String,
  name: String = "tmp${Random.nextLong().absoluteValue}.txt",
  dir: Path = (SYS.pathToUserTmp ?: SYS.pathToSystemTmp ?: "/tmp").toPath(),
): Path {
  val path = dir / name
  SYSTEM.createDirectories(dir)
  SYS.download(url, path)
  return path
}

@OptIn(DelicateApi::class)
private fun CLI.download(url: String, to: Path) {
  // TODO: Add curl to KommandLine library, then use it here
  // -s so no progress bars on error stream; -S to report actual errors on error stream
  val k = kommand("curl", "-s", "-S", "-o", to.toString(), url)
  val result = start(k).waitForResult()
  result.unwrap { err ->
    if (err.isNotEmpty()) {
      ulog.e("FAIL: Error stream was not empty:")
      err.logEach { ulog.e(it) }
      false
    } else true
  }
}

fun downloadAndInjectFileToSpecialRegion(
  inFileUrl: String,
  outFilePath: Path,
  outFileRegionLabel: String,
) {
  val inFilePath = downloadTmpFile(inFileUrl)
  val regionContent = SYSTEM.readUtf8(inFilePath)
  val markBefore = "// region [$outFileRegionLabel]\n"
  val markAfter = "// endregion [$outFileRegionLabel]\n"
  val region = "$markBefore\n$regionContent\n$markAfter"
  SYSTEM.injectCustomRegion(outFileRegionLabel, region, outFilePath)
  SYSTEM.delete(inFilePath)
}
