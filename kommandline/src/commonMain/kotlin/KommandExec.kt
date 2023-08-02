package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


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
    require(isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    require(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
    start(kommand, dir = dir, inFile = inFile, outFile = outFile)
        .awaitResult(inLineS = inLineS)
        .unwrap()
}


// TODO_someday: CliPlatform as context receiver
suspend fun Kommand.exec(
    platform: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = platform.exec(this,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile,
)

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
expect fun Kommand.execb(
    platform: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String>



@Deprecated("Use ReducedKommand.exec(CliPlatform, ...)")
suspend fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut> CliPlatform.exec(
    kommand: ReducedKommand<K, In, Out, Err, TK, ReducedOut>,
    dir: String? = null,
): ReducedOut = kommand.exec(this, dir)

suspend fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut> ReducedKommand<K, In, Out, Err, TK, ReducedOut>
        .exec(platform: CliPlatform, dir: String? = null): ReducedOut = reduce(platform.start(typedKommand, dir))

