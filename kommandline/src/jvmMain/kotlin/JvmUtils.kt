package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

@Deprecated("Use suspend fun Kommand.exec(...)")
fun CliPlatform.execBlocking(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = runBlocking {
    exec(kommand, dir = dir, inContent = inContent, inLineS = inLineS, inFile = inFile, outFile = outFile)
}

// TODO_someday: CliPlatform as context receiver
@Deprecated("Use suspend fun Kommand.exec(...)")
fun Kommand.execBlocking(
    platform: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> = platform.execBlocking(this,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile,
)

// temporary hack
@Deprecated("Use suspend fun Kommand.exec(...)")
actual fun Kommand.execb(
    platform: CliPlatform,
    vararg useNamedArgs: Unit,
    dir: String?,
    inContent: String?,
    inLineS: Flow<String>?,
    inFile: String?,
    outFile: String?,
) = execBlocking(
    platform,
    dir = dir,
    inContent = inContent,
    inLineS = inLineS,
    inFile = inFile,
    outFile = outFile
)


fun Flow<*>.logEachWithMillisBlocking() = runBlocking { logEachWithMillis() }
