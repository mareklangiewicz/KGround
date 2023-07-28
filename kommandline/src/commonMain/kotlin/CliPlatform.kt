package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Deprecated("Use CliPlatform", ReplaceWith("CliPlatform"))
typealias Platform = CliPlatform

interface CliPlatform {

    /**
     * TODO_later: experiment with wrapping some remote (ssh? adb?) platform in sth like bash kommands,
     * so it supports redirect using remote bash operators like < > << >> or sth like that.
     */
    val isRedirectFileSupported: Boolean

    /**
     * @param dir working directory for started subprocess - null means inherit from current process
     * @param inFile - redirect std input from given file - null means do not redirect
     * @param outFile - redirect std output (std err too) to given file - null means do not redirect
     * TODO_maybe: support other redirections (streams/strings with content)
     *   (might require separate flag like: isRedirectStreamsSupported)
     *   (also see comment above at isRedirectContentSupported flag)
     * @param envModify Allows to modify default inherited environment variables for child process.
     *   Can throw exepction if it's unsupported on particular platform.
     */
    fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String? = null,
        inFile: String? = null,
        outFile: String? = null,
        outFileAppend: Boolean = false,
        errToOut: Boolean = false,
        errFile: String? = null,
        errFileAppend: Boolean = false,
        envModify: (MutableMap<String, String>.() -> Unit)? = null,
    ): ExecProcess
    // TODO_maybe: access to input/output/error streams (when not redirected) with Okio source/sink
    // TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719

    // TODO_someday: move it outside as Kommand extension with CliPlatform context receiver
    fun Kommand.exec(
        vararg useNamedArgs: Unit,
        dir: String? = null,
        inContent: String? = null,
        inLines: Sequence<String>? = inContent?.lineSequence(),
        inFile: String? = null,
        outFile: String? = null,
    ): List<String> = exec(this,
        dir = dir,
        inContent = inContent,
        inLines = inLines,
        inFile = inFile,
        outFile = outFile,
    )

    val lineEnd: String get() = "\n"

    val isJvm: Boolean get() = false
    val isDesktop: Boolean get() = false
    val isUbuntu: Boolean get() = false
    val isGnome: Boolean get() = false

    val pathToUserHome get (): String? = null
    val pathToUserTmp get (): String? = null
    val pathToSystemTmp get (): String? = null

    // TODO_someday: access to input/output streams wrapped in okio Source/Sink
    // (but what about platforms running kommands through ssh or adb?)

    companion object {
        val SYS = SysPlatform()
        val FAKE = FakePlatform()
    }
}

fun CliPlatform.exec(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLines: Sequence<String>? = inContent?.lineSequence(),
    inFile: String? = null,
    outFile: String? = null,
): List<String> {
    require(isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
    require(inLines == null || inFile == null) { "Either inLines or inFile or none, but not both" }
    return start(kommand, dir = dir, inFile = inFile, outFile = outFile)
        .waitForResult(inLines = inLines)
        .unwrap()
}

class FakePlatform(
    private val checkStart: (Kommand, String?, String?, String?, Boolean, Boolean, String?, Boolean) -> Unit =
        {_, _, _, _, _, _, _, _ -> },
    private val log: (Any?) -> Unit = ::println): CliPlatform {

    override val isRedirectFileSupported get() = true // not really, but it's all fake

    @DelicateKommandApi
    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?,
        outFileAppend: Boolean,
        errToOut: Boolean,
        errFile: String?,
        errFileAppend: Boolean,
        envModify: (MutableMap<String, String>.() -> Unit)?
    ): ExecProcess {
        log("start($kommand, $dir, ...)")
        checkStart(kommand, dir, inFile, outFile, outFileAppend, errToOut, errFile, errFileAppend)
        return FakeProcess(log)
    }
}

@DelicateKommandApi
class FakeProcess(private val log: (Any?) -> Unit = ::println): ExecProcess {
    override fun waitForExit(finallyClose: Boolean) = 0
    override suspend fun awaitExit(finallyClose: Boolean): Int = waitForExit(finallyClose)
    override fun kill(forcibly: Boolean) = log("cancel($forcibly)")
    override fun close() = Unit
    override fun stdinWriteLine(line: String, lineEnd: String, thenFlush: Boolean): Unit = log("input line: $line")
    override fun stdinClose() = Unit
    override fun stdoutReadLine() = null
    override fun stdoutClose() = Unit
    override fun stderrReadLine() = null
    override fun stderrClose() = Unit
    override suspend fun stdin(
        lineS: Flow<String>,
        lineEnd: String,
        flushAfterEachLine: Boolean,
        finallyStdinClose: Boolean
    ) {
        try { lineS.collect { stdinWriteLine(it, lineEnd, flushAfterEachLine) } }
        finally { if (finallyStdinClose) stdinClose() }
    }
    override val stdout: Flow<String> = stdFakeFlow(::stdoutReadLine, ::stdoutClose)
    override val stderr: Flow<String> = stdFakeFlow(::stderrReadLine, ::stderrClose)
}

private fun stdFakeFlow(readLine: () -> String?, close: () -> Unit): Flow<String> =
    flow { while (true) emit(readLine() ?: break) }.onCompletion { close() }

expect class SysPlatform(): CliPlatform


/**
 * Methods marked DelicateKommandApi are NOT thread safe! Use other ones.
 * Impl notes: Careful with threads. Especially delicate are std streams.
 * Each should use separate thread to avoid strange deadlocks with external process.
 * For example see:
 * https://wiki.sei.cmu.edu/confluence/display/java/FIO07-J.+Do+not+let+external+processes+block+on+IO+buffers
 */
@OptIn(ExperimentalStdlibApi::class)
interface ExecProcess : AutoCloseable {

    /**
     * Tries to kill/destroy/cancel the process. Might not work immediately!
     * @param forcibly - Hint to do it less politely. Some platforms can ignore the hint.
     */
    fun kill(forcibly: Boolean = false)

    suspend fun awaitExit(finallyClose: Boolean = true): Int

    suspend fun stdin(
        lineS: Flow<String>,
        lineEnd: String = CliPlatform.SYS.lineEnd,
        flushAfterEachLine: Boolean = true,
        finallyStdinClose: Boolean = true,
    )

    val stdout: Flow<String>

    val stderr: Flow<String>

    // TODO_maybe: move all delicate methods below to separate interface: (Delicate/Unsafe/LowLevel)ExecProcess

    @DelicateKommandApi
    fun waitForExit(finallyClose: Boolean = true): Int

    /** System.lineSeparator() is added automatically after each input line, so input lines should NOT contain them! */
    @DelicateKommandApi
    fun stdinWriteLine(line: String, lineEnd: String = CliPlatform.SYS.lineEnd, thenFlush: Boolean = true)

    /** Indepotent. Flushes buffer before closing. */
    @DelicateKommandApi
    fun stdinClose()

    /** @return null means end of stream */
    @DelicateKommandApi
    fun stdoutReadLine(): String?

    /** Indepotent. */
    @DelicateKommandApi
    fun stdoutClose()

    /** @return null means end of stream */
    @DelicateKommandApi
    fun stderrReadLine(): String?

    /** Indepotent. */
    @DelicateKommandApi
    fun stderrClose()
}

/** Catches exception thrown by BufferedReader.java:ensureOpen */
fun Flow<String>.catchStreamClosed() =
    catch { it::class.simpleName == "IOException" && it.message == "Stream closed" || throw it }

/**
 * Can be used only once. It always finally closes input stream.
 * System.lineSeparator() is added automatically after each input line, so input lines should NOT contain them!
 */
@DelicateKommandApi
@Deprecated("Use stdin.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.useInLines(input: Sequence<String>, flushAfterEachLine: Boolean = true) =
    try { input.forEach { stdinWriteLine(it, thenFlush = flushAfterEachLine) } }
    finally { stdinClose() }

/** Can be used only once. It always finally closes output stream. */
@DelicateKommandApi
@Deprecated("Use stout Flow.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.useOutLines(block: (output: Sequence<String>) -> Unit) =
    useSomeLines(block, ::stdoutReadLine, ::stdoutClose)

/** Can be used only once. It always finally closes error stream. */
@DelicateKommandApi
@Deprecated("Use sterr Flow.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.useErrLines(block: (output: Sequence<String>) -> Unit) =
    useSomeLines(block, ::stderrReadLine, ::stderrClose)

@DelicateKommandApi
private fun useSomeLines(block: (output: Sequence<String>) -> Unit, readLine: () -> String?, close: () -> Unit) =
    try { block(generateSequence(readLine)) }
    finally { close() }

@DelicateKommandApi
@Deprecated("Use stout Flow.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.useOutLinesOrEmptyIfClosed(block: (output: Sequence<String>) -> Unit) =
    useSomeLinesOrEmptyIfClosed(block, ::useOutLines)

@DelicateKommandApi
@Deprecated("Use sterr Flow.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.useErrLinesOrEmptyIfClosed(block: (error: Sequence<String>) -> Unit) =
    useSomeLinesOrEmptyIfClosed(block, ::useErrLines)

@DelicateKommandApi
private fun useSomeLinesOrEmptyIfClosed(block: (output: Sequence<String>) -> Unit, useLines: ((Sequence<String>) -> Unit) -> Unit) {
    try { useLines(block) }
    catch (e: Exception) {
        if (e.message == "Stream closed") block(emptySequence())
        else throw e
    }
}

// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
/**
 * Not only awaits for process to exit, but collects all output in lists.
 * Then returns both exit code and all output in ExecResult.
 */
suspend fun ExecProcess.awaitResult(
    inContent: String? = null,
    inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow()
): ExecResult = coroutineScope {
    val inJob = inLineS?.let { launch { stdin(it) } }
    val outDeferred = async { stdout.catchStreamClosed().toList() }
    val errDeferred = async { stderr.catchStreamClosed().toList() }
    inJob?.join()
    val out = outDeferred.await()
    val err = errDeferred.await()
    val exit = awaitExit()
    ExecResult(exit, out, err)
}

// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
/**
 * Not only waits for process to exit, but collects all output in lists.
 * Then returns both exit code and all output in ExecResult.
 * Warning: it sequencially tries to consume input first, then output, then err stream.
 * It's much better to use suspend ExecProcess.awaitResult which uses async flows.
 */
@DelicateKommandApi
@Deprecated("Use suspending awaitResult.") // do not remove it - it's here as kinda "educational" example
fun ExecProcess.waitForResult(
    inContent: String? = null,
    inLines: Sequence<String>? = inContent?.lineSequence(),
): ExecResult {
    inLines?.let(::useInLines)
    val out = buildList<String> { useOutLinesOrEmptyIfClosed { addAll(it) } }
    val err = buildList<String> { useErrLinesOrEmptyIfClosed { addAll(it) } }
    // FIXME: is there any chance it's correct to collect err SEQUENCIALLY AFTER collecting whole out?
    //    What happens when we are still collecting output, but subprocess send a LOT of error,
    //    so stderr pipe buffer is full?? Probably subprocess is blocked on writing to err,
    //    and we are here blocked still collecting still open output stream??
    //    if that's the case: it would be much better to make subprocess crash and we throw sth,
    //    instead os such deadlock..
    val exit = waitForExit()
    return ExecResult(exit, out, err)
}

/**
 * Represents full result of execution an external process.
 * @param exit The exit value of the process.
 * @param out The standard output of the process.
 * @param err The standard error of the process.
 */
data class ExecResult(val exit: Int, val out: List<String>, val err: List<String>)

/**
 * Returns the output but ensures the exit value was as expected (0 by default) first.
 * Also by default ensures collected error stream was empty.
 * @throws IllegalStateException if not expected result encounted.
 */
fun ExecResult.unwrap(
    expectedExit: Int? = 0,
    expectedErr: ((List<String>) -> Boolean)? = { it.isEmpty() }
): List<String> {
    expectedExit == null || exit == expectedExit || error("Exit value $exit is not equal to expected $expectedExit")
    expectedErr == null || expectedErr(err) || error("Error stream is not equal to expected error stream.")
    return out
}

fun ExecResult.check(
    expectedExit: Int? = 0,
    expectedErr: ((List<String>) -> Boolean)? = { it.isEmpty()},
    expectedOut: ((List<String>) -> Boolean)?,
) {
    unwrap(expectedExit, expectedErr)
    expectedOut == null || expectedOut(out) || error("Error stream is not equal to expected error stream.")
}

