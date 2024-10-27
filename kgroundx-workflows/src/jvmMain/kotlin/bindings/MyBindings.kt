
package pl.mareklangiewicz.kgroundx.bindings

import io.github.typesafegithub.workflows.actionbindinggenerator.generation.generateBinding
import io.github.typesafegithub.workflows.actionbindinggenerator.domain.ActionCoords
import io.github.typesafegithub.workflows.actionbindinggenerator.domain.MetadataRevision
import okio.Path
import okio.Path.Companion.toPath
import pl.mareklangiewicz.kground.io.localUFileSys
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.localULog
import pl.mareklangiewicz.ulog.w

suspend fun ActionCoords.tryGenerateBindingsToDir(
  metadataRevision: MetadataRevision,
  outputDir: Path,
  askInteractively: Boolean = true,
) {
    val log = localULog()
    val fs = localUFileSys()
    generateBinding(metadataRevision).forEach { binding ->
      val outputFile = outputDir / binding.filePath.toPath().name
      log.i("Trying to write binding to: $outputFile")
      val exists = fs.exists(outputFile)
      if (!exists || !askInteractively || zenityAskIf("Overwrite: $outputFile ?").ax()) {
        writeFileWithDD(binding.kotlinCode.lines(), outputFile).ax()
        if (exists) log.w("Overwritten.") else log.i("Written.")
      }
      else log.w("Skipped.")
    }
}

