package pl.mareklangiewicz.kgroundx.maintenance

import kotlin.math.*
import kotlin.random.*
import okio.*
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.ulog.e
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ure.*

// FIXME NOW separate it from Ure and move Ure common code to better places (later to separate lib)

@OptIn(NotPortableApi::class)
fun Path.injectSpecialRegion(
  regionLabel: String,
  region: String,
  fs: FileSystem = SYSTEM,
  addIfNotFound: Boolean = true,
) {
  val regex = ureWithSpecialRegion(regionLabel).compile()
  fs.processFile(this, this) { output ->
    val outputMR = regex.matchEntire(output)
    if (outputMR == null) {
      ulog.i("Inject [$regionLabel] to $this - No match.")
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
      ulog.i("Inject [$regionLabel] to $this - $summary")
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
private fun CLI.download(url: String, to: Path, log: ULog = ulog) {
  // TODO: Add curl to KommandLine library, then use it here
  // -s so no progress bars on error stream; -S to report actual errors on error stream
  val k = kommand("curl", "-s", "-S", "-o", to.toString(), url)
  val result = start(k).waitForResult()
  result.unwrap { err ->
    if (err.isNotEmpty()) {
      log.e("FAIL: Error stream was not empty:")
      err.logEach(log, ULogLevel.ERROR)
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
  outFilePath.injectSpecialRegion(outFileRegionLabel, region)
  SYSTEM.delete(inFilePath)
}
