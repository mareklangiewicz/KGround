package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.bad.*


// TODO_someday: CLI as context parameter
//  for now convention is: first parameter "cli: CLI", because it's the same as kgroundxio:WithCLI interface
/**
 * The ax Awaits/eXecutes the kommand. And it's dangerous :)
 * It has to be explicitly called for every kommand in the "script" so has to be short "keyword" to memorize.
 * TODO_someday: how to colorize it (can I somehow use @DslMarker ?)
 */
suspend fun Kommand.ax(
  vararg useNamedArgs: Unit,
  dir: String? = null,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: String? = null,
  outFile: String? = null,
): List<String> = coroutineScope {
  val cli = implictx<CLI>()
  req(cli.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
  req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
  cli.lx(this@ax, dir = dir, inFile = inFile, outFile = outFile)
    .awaitResult(inLineS = inLineS)
    .unwrap()
}



