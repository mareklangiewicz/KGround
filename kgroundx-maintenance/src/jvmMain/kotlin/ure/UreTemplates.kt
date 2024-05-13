package pl.mareklangiewicz.kgroundx.maintenance

import okio.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.io.*
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.implictx
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*

// FIXME NOW separate it from Ure and move Ure common code to better places (later to separate lib)

@OptIn(NotPortableApi::class)
suspend fun Path.injectSpecialRegion(
  regionLabel: String,
  region: String,
  addIfNotFound: Boolean = true,
) {
  val log = implictx<ULog>()
  val regex = ureWithSpecialRegion(regionLabel).compile()
  processFile(this, this) { output ->
    val outputMR = regex.matchEntire(output)
    if (outputMR == null) {
      log.i("Inject [$regionLabel] to $this - No match.")
      if (addIfNotFound) {
        log.i("Adding new region at the end.")
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
      log.i("Inject [$regionLabel] to $this - $summary")
      newOutput
    }
  }
}

suspend fun Path.injectSpecialRegionContentFromFile(
  regionLabel: String,
  regionContentFile: Path,
  addIfNotFound: Boolean = true,
  regionContentMap: suspend (String) -> String = { "// region [$regionLabel]\n\n$it\n// endregion [$regionLabel]\n" },
) {
  val regionContent = implictx<UFileSys>().readUtf8(regionContentFile)
  val region = regionContentMap(regionContent)
  injectSpecialRegion(regionLabel, region, addIfNotFound)
}

@OptIn(DelicateApi::class)
suspend fun downloadAndInjectFileToSpecialRegion(
  inFileUrl: String,
  outFilePath: Path,
  outFileRegionLabel: String,
) {
  val inFilePath = curlDownloadTmpFile(inFileUrl)
  outFilePath.injectSpecialRegionContentFromFile(outFileRegionLabel, inFilePath)
  implictx<UFileSys>().delete(inFilePath)
}
