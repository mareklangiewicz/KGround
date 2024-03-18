package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
actual fun Kommand.execb(
    cli: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String?,
    inContent: String?,
    inLineS: Flow<String>?,
    inFile: String?,
    outFile: String?,
): List<String> = TODO("Remove this functionality")

// also temporary hack
@Deprecated("Use suspend fun ReducedKommand.exec(...)")
actual fun <ReducedOut> ReducedScript<ReducedOut>.execb(cli: CliPlatform, dir: String?): ReducedOut =
    TODO("Remove this functionality")
