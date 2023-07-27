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
    val stdinRetype: FlowCollector<String>.() -> In,
    val stderrRetype: Flow<String>.() -> Err,
    val stderrToOut: Boolean,
    val stdoutRetype: Flow<String>.() -> Out,
)

fun <K: Kommand, In, Out, Err> K.typed(
    stdinRetype: FlowCollector<String>.() -> In,
    stderrRetype: Flow<String>.() -> Err,
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
) = TypedKommand(this, stdinRetype, stderrRetype, stderrToOut, stdoutRetype)

fun <K: Kommand, In, Out> K.typed(
    stdinRetype: FlowCollector<String>.() -> In,
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
): TypedKommand<K, In, Out, Flow<Nothing>> =
    typed(stdinRetype, { map { error("Unexpected error: $it") } }, stderrToOut, stdoutRetype)

fun <K: Kommand, Out> K.typed(
    stderrToOut: Boolean = false,
    stdoutRetype: Flow<String>.() -> Out,
): TypedKommand<K, Unit, Out, Flow<Nothing>> =
    typed({}, stderrToOut, stdoutRetype)

class TypedExecProcess<In, Out, Err>(
    private val eprocess: ExecProcess,
    stdinRetype: FlowCollector<String>.() -> In,
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

suspend fun <K: Kommand, In, Out, Err, TK: TypedKommand<K, In, Out, Err>, ReducedOut> CliPlatform.exec(
    kommand: ReducedKommand<K, In, Out, Err, TK, ReducedOut>,
    dir: String? = null,
): ReducedOut = kommand.reduce(start(kommand.typedKommand, dir))
