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
    ): List<String> =
        exec(this, dir = dir, inContent = inContent, inLines = inLines, inFile = inFile, outFile = outFile)

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
    private val checkStart: (Kommand, String?, String?, String?) -> Unit = { _, _, _, _ -> },
    private val log: (Any?) -> Unit = ::println): CliPlatform {

    override val isRedirectFileSupported get() = true // not really, but it's all fake

    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?,
        envModify: (MutableMap<String, String>.() -> Unit)?,
    ): ExecProcess {
        log("start($kommand, $dir)")
        checkStart(kommand, dir, inFile, outFile)
        return FakeProcess(log)
    }
}

class FakeProcess(private val log: (Any?) -> Unit = ::println): ExecProcess {
    override fun waitForExit() = 0
    override fun cancel(force: Boolean) = log("cancel($force)")
    override fun useInputLines(input: Sequence<String>) = input.forEach { log("input line: $it") }
    override fun useOutputLines(block: (output: Sequence<String>) -> Unit) = block(emptySequence())
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
    fun useInputLines(input: Sequence<String>)

    /** Can be used only once. It always finally close output stream. */
    fun useOutputLines(block: (output: Sequence<String>) -> Unit)
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
    inLines?.let(::useInputLines)
    val output = buildList<String> { useOutputLines { addAll(it) } }
    val exit = waitForExit()
    return ExecResult(exit, output)
}

fun ExecProcess.pushEachOutuptLine(pushee: Pushee<String>) = useOutputLines { it.forEach { pushee(it)} }

fun ExecProcess.pullEachInputLine(pullee: Pullee<String>) = useInputLines(pullee.iterator().asSequence())

/**
 * Represents the result of execution an external process.
 * @param exitValue The exit value of the process.
 * @param stdOutAndErr The standard output and error of the process, combined in a single list of strings.
 * It's better to always have std out and err merged, so it's always clear after which output there was an error.
 * Also, KommandLine is generally wrapping commands in functions, so it's more composable to have just one output.
 */
data class ExecResult(val exitValue: Int, val stdOutAndErr: List<String>)

/**
 * Returns the output but ensures the exit value was as expected (0 by default) first
 * @throws IllegalStateException if exit value is not equal to expectedExitValue
 */
fun ExecResult.unwrap(expectedExitValue: Int = 0): List<String> =
    if (exitValue == expectedExitValue) stdOutAndErr
    else {
        println(stdOutAndErr)
        val message = "Exit value $exitValue is not equal to expected $expectedExitValue."
        println(message)
        error(message)
    }

fun ExecResult.check(expectedExitValue: Int = 0, expectedOutput: List<String>? = null) {
    val actualOutput = unwrap(expectedExitValue) // makes sure we first check exit value
    check(expectedOutput == null || expectedOutput == actualOutput)
}

