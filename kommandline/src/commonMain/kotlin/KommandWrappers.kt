@file:Suppress("unused")

package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.DelicateApi

/**
 * Separate from Kommand, because I want Kommand to be simple and serializable!
 * TODO_someday_maybe: Use kotlinx-serialization for all Kommands (but not TypedKommands)
 * Note1: TypedKommand assumes no redirections at CLI level.
 * Instead, just compose stdin/out/err data types (usually flows) (it will result in better code in most cases).
 * If CLI level redirections are really needed, then just use lower level api.
 * But first, consider if you can just locally save/load flows to/from files using Okio.
 * (Overall goal is to gradually move AWAY from CLI craziness and more towards safe/composable kotlin programming.)
 */
data class TypedKommand<out K: Kommand, In, Out, Err>(
    val kommand: K,
    val stdinRetype: StdinCollector.() -> In,
    val stderrRetype: Flow<String>.() -> Err,
    val stderrToOut: Boolean,
    val stdoutRetype: Flow<String>.() -> Out,
)

fun <K: Kommand, In, Out, Err> K.typed(
    stdinRetype: StdinCollector.() -> In,
    stderrRetype: Flow<String>.() -> Err,
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
) = TypedKommand(this, stdinRetype, stderrRetype, stderrToOut, stdoutRetype)

// these default retype algorithms/vals are defined here mostly for me to be able to compare when debugging/testing
internal val defaultInRetypeToItSelf: StdinCollector.() -> StdinCollector = { this }
internal val defaultOutRetypeToItSelf: Flow<String>.() -> Flow<String> = { this }

fun <K: Kommand, Out> K.typed(
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
): TypedKommand<K, StdinCollector, Out, Flow<String>> =
    typed(
        stdinRetype = defaultInRetypeToItSelf,
        stderrRetype = defaultOutRetypeToItSelf,
        stderrToOut = stderrToOut,
        stdoutRetype = stdoutRetype
    )

class TypedExecProcess<In, Out, Err>(
    private val eprocess: ExecProcess,
    stdinRetype: StdinCollector.() -> In,
    stderrRetype: Flow<String>.() -> Err,
    stdoutRetype: Flow<String>.() -> Out,
) {
    fun kill(forcibly: Boolean = false) = eprocess.kill(forcibly)
    suspend fun awaitExit(finallyClose: Boolean = true) = eprocess.awaitExit(finallyClose)
    val stdin: In = eprocess.stdin.stdinRetype()
    val stdout: Out = eprocess.stdout.stdoutRetype()
    val stderr: Err = eprocess.stderr.stderrRetype()
}

suspend fun TypedExecProcess<*, *, Flow<String>>.awaitAndChkExit(
    expExit: Int = 0,
    firstCollectErr: Boolean,
    finallyClose: Boolean = true
) {
    val collectedErr: List<String>? = if (firstCollectErr) stderr.toList() else null
    awaitExit(finallyClose).chkExit(expExit, collectedErr)
}

/**
 * If unexpected exit, then it will normally throw [BadExitStateErr], but with stderr set to null (meaning: unknown).
 * Usually it's better to capture stderr in some way, so think twice before choosing this extension function.
 */
@DelicateApi
suspend fun TypedExecProcess<*, *, *>.awaitAndChkExitIgnoringStdErr(
    expExit: Int = 0,
    finallyClose: Boolean = true
) = awaitExit(finallyClose).chkExit(expExit)

/**
 * @param dir working directory for started subprocess - null means inherit from the current process
 */
fun <K: Kommand, In, Out, Err> CLI.start(
    kommand: TypedKommand<K, In, Out, Err>,
    dir: String? = null,
) = TypedExecProcess(
    eprocess = start(kommand = kommand.kommand, dir = dir, errToOut = kommand.stderrToOut),
    stdinRetype = kommand.stdinRetype,
    stderrRetype = kommand.stderrRetype,
    stdoutRetype = kommand.stdoutRetype
)
// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719


// TODO_someday: (When we have context receivers in MPP and it's time for bigger refactor):
//   <Update> I introduced ReducedScript as a coy experiment already... will see </Update>
//   this ReducedKommand interface is in fact more general contract - sth like "ReducedScript",
//   that should also represent executing more kommands on some platform, not just one.
//   Rethink if I need both fun interface ReducedScript, and just empty interface ReducedKommand : ReducedScript,
//   or maybe ReducedScript is even enough and ReducedKommand could be deleted.
//   Then ReducedKommandMap, etc. would also be just a specific form of ReducedScript.

fun interface ReducedScript<ReducedOut> {
    // TODO_maybe: dir should probably be inside CLI as val currentDir.
    //   and maybe sth like CLI.withCurrentDir(dir, code:...) (or rather with context receivers)
    suspend fun exec(cli: CLI, dir: String?): ReducedOut
    // TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
}

suspend fun <ReducedOut> ReducedScript<ReducedOut>.exec(cli: CLI) = exec(cli, null)

interface ReducedKommand<ReducedOut> : ReducedScript<ReducedOut>

internal class ReducedKommandImpl<K: Kommand, In, Out, Err, ReducedOut>(
    val typedKommand: TypedKommand<K, In, Out, Err>,
    val reduce: suspend TypedExecProcess<In, Out, Err>.() -> ReducedOut,
): ReducedKommand<ReducedOut> {
    override suspend fun exec(cli: CLI, dir: String?): ReducedOut = reduce(cli.start(typedKommand, dir))
}

internal class ReducedKommandMap<InnerOut, MappedOut>(
    val reducedKommand: ReducedKommand<InnerOut>,
    val reduceMap: suspend InnerOut.() -> MappedOut,
): ReducedKommand<MappedOut> {
    override suspend fun exec(cli: CLI, dir: String?): MappedOut = reducedKommand.exec(cli, dir).reduceMap()
}

fun <InnerOut, MappedOut> ReducedKommand<InnerOut>.reducedMap(
    reduceMap: suspend InnerOut.() -> MappedOut,
): ReducedKommand<MappedOut> = ReducedKommandMap(this, reduceMap)


/** Mostly for tests to try to compare wrapped kommand line to expected line. */
@DelicateApi
fun ReducedKommand<*>.lineRawOrNull(): String? = when (this) {
    is ReducedKommandImpl<*, *, *, *, *> -> typedKommand.kommand.lineRaw()
    is ReducedKommandMap<*, *>  -> reducedKommand.lineRawOrNull()
    else -> null
}


/** Note: Manually means: user is responsible for collecting all necessary streams and awaiting and checking exit. */
fun <K: Kommand, In, Out, Err, ReducedOut> TypedKommand<K, In, Out, Err>.reducedManually(
    reduceManually: suspend TypedExecProcess<In, Out, Err>.() -> ReducedOut,
): ReducedKommand<ReducedOut> = ReducedKommandImpl(this, reduceManually)

/** Note: Manually means: user is responsible for collecting all necessary streams and awaiting and checking exit. */
fun <K: Kommand, ReducedOut> K.reducedManually(
    reduceManually: suspend TypedExecProcess<StdinCollector, Flow<String>, Flow<String>>.() -> ReducedOut,
): ReducedKommand<ReducedOut> = typed(stdoutRetype = defaultOutRetypeToItSelf)
    .reducedManually(reduceManually)

/**
 * Note: reduceOut means: user is responsible only for reducing stdout;
 * stderr and exit will be handled in the default way; stdin will not be used at all.
 */
fun <K: Kommand, In, Out, ReducedOut> TypedKommand<K, In, Out, Flow<String>>.reducedOut(
    reduceOut: suspend Out.() -> ReducedOut,
): ReducedKommand<ReducedOut> = ReducedKommandImpl(this) {
    coroutineScope {
        val deferredErr = async { stderr.toList() }
        val reducedOut = stdout.reduceOut()
        val collectedErr = deferredErr.await()
        awaitExit().chkExit(stderr = collectedErr)
        reducedOut
    }
}

fun <K: Kommand, ReducedOut> K.reducedOut(
    reduceOut: suspend Flow<String>.() -> ReducedOut,
): ReducedKommand<ReducedOut> = this
    .typed(stdoutRetype = defaultOutRetypeToItSelf)
    .reducedOut(reduceOut)

fun <K: Kommand, ReducedExit> K.reducedExit(
    reduceExit: suspend (Int) -> ReducedExit,
): ReducedKommand<ReducedExit> = this
    .typed(stdoutRetype = defaultOutRetypeToItSelf)
    .reducedManually { reduceExit(awaitExit()) }


// These four below look unnecessary, but I like how they explicitly suggest common correct thing to do in the IDE.

fun <K: Kommand> K.reducedOutToUnit(): ReducedKommand<Unit> = reducedOut {}
fun <K: Kommand> K.reducedOutToList(): ReducedKommand<List<String>> = reducedOut { toList() }
fun <K: Kommand> K.reducedOutToFlow(): ReducedKommand<Flow<String>> =
    reducedManually { stdout.onCompletion { awaitAndChkExit(firstCollectErr = false) } }

fun <In, OutItem> TypedKommand<*, In, Flow<OutItem>, Flow<String>>.reducedOutToList(): ReducedKommand<List<OutItem>> =
    reducedOut { toList() }

fun <In, Out> TypedKommand<*, In, Out, Flow<String>>.reducedOutToUnit(): ReducedKommand<Unit> =
    reducedOut {}

fun <In, OutItem> TypedKommand<*, In, Flow<OutItem>, Flow<String>>.reducedOutToFlow(): ReducedKommand<Flow<OutItem>> =
    reducedManually { stdout.onCompletion { awaitAndChkExit(firstCollectErr = false) } }

