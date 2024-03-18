package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.bad.*


@Deprecated("Use Kommand.exec(CliPlatform, ...)")
suspend fun CliPlatform.exec(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = coroutineScope {
    req(isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
    start(kommand, dir = dir, inFile = inFile, outFile = outFile)
        .awaitResult(inLineS = inLineS)
        .unwrap()
}


// TODO_someday: CliPlatform as context receiver
//  for now convention is: first parameter "cli: CliPlatform", because it's the same as kgroundxio:WithCLI interface
suspend fun Kommand.exec(
    cli: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = cli.exec(this,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile,
)

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
expect fun Kommand.execb(
    cli: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String>

// also temporary hack
expect fun <ReducedOut> ReducedScript<ReducedOut>.execb(cli: CliPlatform, dir: String? = null): ReducedOut

@Deprecated("Use ReducedKommand.exec(CliPlatform, ...)")
suspend fun <ReducedOut> CliPlatform.exec(kommand: ReducedKommand<ReducedOut>, dir: String? = null): ReducedOut =
    kommand.exec(this, dir)


