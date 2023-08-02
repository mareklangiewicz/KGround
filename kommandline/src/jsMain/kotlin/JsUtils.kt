package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*

// temporary hack
actual fun Kommand.execb(
    platform: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String?,
    inContent: String?,
    inLineS: Flow<String>?,
    inFile: String?,
    outFile: String?,
): List<String> = TODO("Remove this functionality")
