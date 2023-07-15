package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.upue.*

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
    // TODO_maybe: support for outFile appending (java:ProcessBuilder.Redirect.appendTo)
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

    /**
     * WARNING: Current impl first wait for process to read whole input (blocking) and then starts to consume output.
     * If it deadlocks, that is why.. FIXME: Use coroutines and Flow instead of Sequence?? (+add thread safety by default)
     */
    @DelicateKommandApi
    fun Kommand.execonsume(
        vararg useNamedArgs: Unit,
        dir: String? = null,
        inContent: String? = null,
        inLines: Sequence<String>? = inContent?.lineSequence(),
        inFile: String? = null,
        outLinesConsumer: (outLines: Sequence<String>) -> Unit,
    ) = execonsume(
        this,
        dir = dir,
        inContent = inContent,
        inLines = inLines,
        inFile = inFile,
        outLinesConsumer = outLinesConsumer
    )

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

/**
 * WARNING: Current impl first wait for process to read whole input (blocking) and then starts to consume output.
 * If it deadlocks, that is why.. FIXME: Use coroutines and Flow instead of Sequence?? (+add thread safety by default)
 */
@DelicateKommandApi
fun CliPlatform.execonsume(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    dir: String? = null,
    inContent: String? = null,
    inLines: Sequence<String>? = inContent?.lineSequence(),
    inFile: String? = null,
    outLinesConsumer: (outLines: Sequence<String>) -> Unit,
) {
    require(isRedirectFileSupported || (inFile == null)) { "redirect file not supported here" }
    require(inLines == null || inFile == null) { "Either inLines or inFile or none, but not both" }
    start(kommand, dir = dir, inFile = inFile)
        .apply {
            inLines?.let(::useInLines)
            useOutLines { outLinesConsumer(it) }
        }
        .waitForResult() // inLines already consumed (outLines too BTW)
        .check { it.isEmpty() }
}

class FakePlatform(
    private val checkStart: (Kommand, String?, String?, String?, Boolean, Boolean, String?, Boolean) -> Unit =
        {_, _, _, _, _, _, _, _ -> },
    private val log: (Any?) -> Unit = ::println): CliPlatform {

    override val isRedirectFileSupported get() = true // not really, but it's all fake

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

class FakeProcess(private val log: (Any?) -> Unit = ::println): ExecProcess {
    override fun waitForExit() = 0
    override fun cancel(force: Boolean) = log("cancel($force)")
    override fun useInLines(input: Sequence<String>) = input.forEach { log("input line: $it") }
    override fun useOutLines(block: (output: Sequence<String>) -> Unit) = block(emptySequence())
    override fun useErrLines(block: (error: Sequence<String>) -> Unit) = block(emptySequence())
}

expect class SysPlatform(): CliPlatform


interface ExecProcess {

    fun waitForExit(): Int

    /**
     * Tries to cancel (destroy) the process. May not work immediately
     * @param force - Hint to do it less politely. Some platforms can ignore the hint.
     */
    fun cancel(force: Boolean)

    /**
     * Can be used only once. It always finally close input stream.
     * System.lineSeparator() is added automatically after each input line, so input lines should NOT contain them!
     */
    fun useInLines(input: Sequence<String>)

    /** Can be used only once. It always finally close output stream. */
    fun useOutLines(block: (output: Sequence<String>) -> Unit)
    /** Can be used only once. It always finally close error stream. */
    fun useErrLines(block: (error: Sequence<String>) -> Unit)
}

fun ExecProcess.useOutLinesOrEmptyIfClosed(block: (output: Sequence<String>) -> Unit) {
    try { useOutLines(block) }
    catch (e: Exception) {
        if (e.message == "Stream closed") block(emptySequence())
        else throw e
    }
}

fun ExecProcess.useErrLinesOrEmptyIfClosed(block: (error: Sequence<String>) -> Unit) {
    try { useErrLines(block) }
    catch (e: Exception) {
        if (e.message == "Stream closed") block(emptySequence())
        else throw e
    }
}

// TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719
/**
 * Not only waits for process to exit, but collects all output in a list.
 * Then returns both exit code and all output in ExecResult.
 */
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

fun ExecProcess.pushEachOutLine(pushee: Pushee<String>) = useOutLines { it.forEach { pushee(it)} }

fun ExecProcess.pullEachInLine(pullee: Pullee<String>) = useInLines(pullee.iterator().asSequence())

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

