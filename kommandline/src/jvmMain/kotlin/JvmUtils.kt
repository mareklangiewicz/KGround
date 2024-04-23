package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kground.*

// TODO_someday: CLI as context receiver
@Deprecated("Use suspend fun Kommand.ax(...)")
fun Kommand.axBlocking(
  cli: CLI = CLI.SYS,
  vararg useNamedArgs: Unit,
  dir: String? = null,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: String? = null,
  outFile: String? = null,
): List<String> = runBlocking {
  ax(cli, dir = dir, inContent = inContent, inLineS = inLineS, inFile = inFile, outFile = outFile)
}

// temporary hack
@Deprecated("Use suspend fun Kommand.ax(...)")
actual fun Kommand.axb(
  cli: CLI,
  vararg useNamedArgs: Unit,
  dir: String?,
  inContent: String?,
  inLineS: Flow<String>?,
  inFile: String?,
  outFile: String?,
) = axBlocking(
  cli,
  dir = dir,
  inContent = inContent,
  inLineS = inLineS,
  inFile = inFile,
  outFile = outFile,
)

// also temporary hack
@Deprecated("Use suspend fun ReducedKommand.ax(...)")
actual fun <ReducedOut> ReducedScript<ReducedOut>.axb(cli: CLI, dir: String?): ReducedOut =
  runBlocking { ax(cli, dir) }


fun Flow<*>.logEachBlocking() = runBlocking { logEach() }
