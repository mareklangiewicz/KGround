package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import org.jetbrains.kotlinx.jupyter.api.*
import org.jetbrains.kotlinx.jupyter.api.libraries.*

internal class KommandIntegration: JupyterIntegration() {
    override fun Builder.onLoaded() {
//        render<BlaBla> { HTML("<p><b>bla1: </b>${it.bla1}</p><p><b>bla2: </b>${it.bla2}</p>") }
        import("kotlinx.coroutines.*")
        import("kotlinx.coroutines.flow.*")
        import("pl.mareklangiewicz.kommand.*")
        import("pl.mareklangiewicz.kommand.core.*")
        import("pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS")
    }
}


@OptIn(ExperimentalTime::class)
fun Flow<*>.logEachWithMillisBlocking() = runBlocking { logEachWithMillis() }


// I don't want too many shortcut names inside kommand itself, but here it's fine


/**
 * Kinda like .exec, but less strict/explicit, because here in notebook we are in more local "experimental" context.
 *
 * WARNING: Current impl first wait for process to read whole input (blocking) and then starts to consume output.
 * If it deadlocks, that is why.. See CliPlatform.execonsume - same problem
 */
@OptIn(DelicateKommandApi::class, DelicateCoroutinesApi::class)
fun Kommand.x(
    platform: CliPlatform = SYS,
    dir: String? = null,
    vararg useNamedArgs: Unit,
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
    inFile: String? = null,
    outFile: String? = null,
    outFileAppend: Boolean = false,
    errToOut: Boolean = false,
    errFile: String? = null,
    errFileAppend: Boolean = false,
    expectedExit: Int? = 0,
    expectedErr: ((List<String>) -> Boolean)? = { it.isEmpty() },
    outLinesCollector: FlowCollector<String>? = null,
): List<String> = runBlocking {
    require(platform.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    require(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
    require(outLinesCollector == null || outFile == null) { "Either outLinesCollector or outFile or none, but not both" }
    val eprocess = platform.start(this@x,
        dir = dir,
        inFile = inFile,
        outFile = outFile,
        outFileAppend = outFileAppend,
        errToOut = errToOut,
        errFile = errFile,
        errFileAppend = errFileAppend
    )
    val inJob = inLineS?.let { launch { eprocess.stdin.collect(it) }}
    val outJob = outLinesCollector?.let { eprocess.stdout.onEach(it::emit).launchIn(this) }
    inJob?.join()
    outJob?.join()
    eprocess
        .awaitResult() // inLinesFlow already used
        .unwrap(expectedExit, expectedErr)
}
