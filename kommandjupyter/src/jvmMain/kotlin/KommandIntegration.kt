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
        // FIXME_later: refactor packages, so only high-level functions are imported automatically, and delicate api are NOT.
        //   maybe high-level fun stuff up to pl.mareklangiewicz.kommand package??
        import("pl.mareklangiewicz.kommand.*")
        import("pl.mareklangiewicz.kommand.core.*")
        import("pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS")
        import("pl.mareklangiewicz.kommand.find.*")
        import("pl.mareklangiewicz.kommand.github.*")
    }
}


@OptIn(ExperimentalTime::class)
fun Flow<*>.logEachWithMillisBlocking() = runBlocking { logEachWithMillis() }


// I don't want too many shortcut names inside kommand itself, but here it's fine


/**
 * Kinda like .exec, but less strict/explicit, because in notebooks we are in more local "experimental" context.
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

fun <K: Kommand, In, Out, Err> TypedKommand<K, In, Out, Err>.xstart(
    platform: CliPlatform = SYS,
    dir: String? = null,
) = platform.start(this, dir)


suspend fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut, RK: ReducedKommand<K, In, Out, Err, TK, ReducedOut>> RK.x(
    platform: CliPlatform = SYS,
    dir: String? = null,
): ReducedOut = platform.exec(this, dir = dir)

fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut, RK: ReducedKommand<K, In, Out, Err, TK, ReducedOut>> RK.xblocking(
    platform: CliPlatform = SYS,
    dir: String? = null,
): ReducedOut = runBlocking { x(platform, dir) }
