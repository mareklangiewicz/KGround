package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.bad.*


// TODO_someday: CLI as context receiver
//  for now convention is: first parameter "cli: CLI", because it's the same as kgroundxio:WithCLI interface
suspend fun Kommand.exec(
    cli: CLI,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = coroutineScope {
    req(cli.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
    cli.start(this@exec, dir = dir, inFile = inFile, outFile = outFile)
        .awaitResult(inLineS = inLineS)
        .unwrap()
}

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
expect fun Kommand.execb(
    cli: CLI,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String>

// also temporary hack
expect fun <ReducedOut> ReducedScript<ReducedOut>.execb(cli: CLI, dir: String? = null): ReducedOut


