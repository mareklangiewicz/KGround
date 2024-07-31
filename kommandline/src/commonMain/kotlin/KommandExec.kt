package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.bad.*
import okio.Path
import pl.mareklangiewicz.kground.io.localUWorkDirOrNull


/**
 * The ax Awaits/eXecutes the kommand. And it's dangerous :)
 * It has to be explicitly called for every kommand in the "script" so has to be short "keyword" to memorize.
 * TODO_someday: how to colorize it (can I somehow use @DslMarker ?)
 */
suspend fun Kommand.ax(
  vararg useNamedArgs: Unit,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: Path? = null,
  outFile: Path? = null,
): List<String> = coroutineScope {
  val workDir = localUWorkDirOrNull()
  val cli: CLI = localCLI()
  req(cli.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
  req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
  cli.lx(this@ax, workDir = workDir?.dir, inFile = inFile, outFile = outFile)
    .awaitResult(inLineS = inLineS)
    .unwrap()
}



