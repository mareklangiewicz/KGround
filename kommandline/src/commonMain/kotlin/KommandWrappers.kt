package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*


/**
 * Separate from Kommand, because I want Kommand to be simple and serializable!
 * TODO_someday_maybe: Use kotlinx-serialization for all Kommands (but not TypedKommands)
 * Note1: TypedKommand assumes no redirections at platform level.
 * Instead, just compose stdin/out/err data types (usually flows) (it will result in better code in most cases).
 * If platform level redirections are really needed, then just use lower level api.
 * But first, consider if you can just locally save/load flows to/from files using Okio.
 * (Overall goal is to gradually move AWAY from CLI craziness and more towards safe/composable kotlin programming.)
 */
data class TypedKommand<K: Kommand, In, Out, Err>(
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

fun <K: Kommand, In, Out> K.typed(
    stdinRetype: StdinCollector.() -> In,
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
): TypedKommand<K, In, Out, Flow<Nothing>> =
    typed(stdinRetype, defaultOutRetypeAnyLineToUnexpectedError, stderrToOut, stdoutRetype)

// these default retype algorithms/vals are defined here mostly for me to be able to compare when debugging/testing
internal val defaultInRetypeToItSelf: StdinCollector.() -> StdinCollector = { this }
internal val defaultOutRetypeToItSelf: Flow<String>.() -> Flow<String> = { this }
internal val defaultOutRetypeAnyLineToUnexpectedError: Flow<String>.() -> Flow<Nothing> = { map { error("Unexpected: $it") } }

fun <K: Kommand, Out> K.typed(
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
): TypedKommand<K, StdinCollector, Out, Flow<Nothing>> =
    typed(defaultInRetypeToItSelf, stderrToOut, stdoutRetype)

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

/**
 * @param dir working directory for started subprocess - null means inherit from current process
 */
fun <K: Kommand, In, Out, Err> CliPlatform.start(
    kommand: TypedKommand<K, In, Out, Err>,
    dir: String? = null,
) = TypedExecProcess(
    eprocess = start(kommand = kommand.kommand, dir = dir, errToOut = kommand.stderrToOut),
    stdinRetype = kommand.stdinRetype,
    stderrRetype = kommand.stderrRetype,
    stdoutRetype = kommand.stdoutRetype
)
// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719


data class ReducedKommand<K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut>(
    val typedKommand: TK,
    val reduce: suspend TypedExecProcess<In, Out, Err>.() -> ReducedOut,
)

fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut> TK.reduced(
    reduce: suspend TypedExecProcess<In, Out, Err>.() -> ReducedOut,
) = ReducedKommand(this, reduce)

fun <K: Kommand, ReducedOut> K.reduced(
    reduce: suspend TypedExecProcess<StdinCollector, Flow<String>, Flow<Nothing>>.() -> ReducedOut,
): ReducedKommand<K, StdinCollector, Flow<String>, Flow<Nothing>, TypedKommand<K, StdinCollector, Flow<String>, Flow<Nothing>>, ReducedOut> =
    typed(stdoutRetype = defaultOutRetypeToItSelf).reduced(reduce)

/**
 * Another wrapper to use reduced kommands in an even simpler way - as normal suspending functions.
 * And to hide complicated generic types from the user side (maybe it also helps IDE performance).
 * Not sure if it's needed, or maybe I should simplify/hide types in ReducedKommand itself.
 */
@ExperimentalKommandApi
class FunctionKommand<FunctionOut>(
    private val reducedKommand: ReducedKommand<*, *, *, *, *, FunctionOut>,
    private val dir: String? = null,
    private val platform: CliPlatform = CliPlatform.SYS,
): (suspend () -> FunctionOut) {
    override suspend fun invoke(): FunctionOut = reducedKommand.exec(platform, dir)
}

@ExperimentalKommandApi
fun <FunctionOut> ReducedKommand<*, *, *, *, *, FunctionOut>.toFun(
    dir: String? = null,
    platform: CliPlatform = CliPlatform.SYS,
) = FunctionKommand(this, dir, platform)


