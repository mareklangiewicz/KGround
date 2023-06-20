package pl.mareklangiewicz.kommand

@Deprecated("Use CliPlatform", ReplaceWith("CliPlatform"))
typealias Platform = CliPlatform

interface CliPlatform {

    /**
     * TODO_later: experiment with wrapping some remote (ssh? adb?) platform in sth like bash kommands,
     * so it supports redirect using remote bash operators like < > << >> or sth like that.
     */
    val isRedirectFileSupported: Boolean
    val isRedirectContentSupported: Boolean
        // TODO_maybe: more universal redirection via kotlin flows
        // (but wrapping java.lang.Redirect etc correctly can be tricky..)

    /**
     * @param dir working directory for started subprocess - null means inherit from current process
     * @param inFile - redirect std input from given file - null means do not redirect
     * @param outFile - redirect std output (std err too) to given file - null means do not redirect
     * TODO_maybe: support other redirections (streams/strings with content)
     *   (might require separate flag like: isRedirectStreamsSupported)
     *   (also see comment above at isRedirectContentSupported flag)
     */
    fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String? = null,
        inFile: String? = null,
        outFile: String? = null
    ): ExecProcess
    // TODO_later: access to input/output/error streams (when not redirected) with Okio source/sink
    // TODO_later: support for outFile appending (java:ProcessBuilder.Redirect.appendTo)
    // TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719

    // TODO_someday: move it outside as Kommand extension with CliPlatform context receiver
    fun Kommand.exec(
        vararg useNamedArgs: Unit,
        dir: String? = null,
        inContent: String? = null,
        inFile: String? = null,
        outFile: String? = null,
    ): List<String> {
        require(isRedirectContentSupported || inContent == null) { "redirect content not supported here" }
        require(isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
        require(inContent == null || inFile == null) { "Either inContent or inFile or none, but not both" }
        return start(this, dir = dir, inFile = inFile, outFile = outFile).await(inContent).unwrap()
    }

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

class FakePlatform(
    private val checkStart: (Kommand, String?, String?, String?) -> Unit = { _, _, _, _ -> },
    private val checkAwait: (String?) -> Unit = {},
    private val log: (Any?) -> Unit = ::println): CliPlatform {

    override val isRedirectFileSupported get() = true // not really, but it's all fake
    override val isRedirectContentSupported get() = true // not really, but it's all fake

    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?
    ): ExecProcess {
        log("start($kommand, $dir)")
        checkStart(kommand, dir, inFile, outFile)
        return object : ExecProcess {
            override fun await(inContent: String?): ExecResult {
                log("await(..)")
                checkAwait(inContent)
                return ExecResult(0, listOf("fake output")) // many kommand wrappers expect single line output
            }
            override fun cancel(force: Boolean) = log("cancel($force)")
        }
    }
}

expect class SysPlatform(): CliPlatform


interface ExecProcess {
    fun await(inContent: String? = null): ExecResult
    // TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719

    /**
     * Tries to cancel (destroy) the process. May not work immediately
     * @param force - Hint to do it less politely. Some platforms can ignore the hint.
     */
    fun cancel(force: Boolean)
}

data class ExecResult(val exitValue: Int, val stdOutAndErr: List<String>)

/**
 * Returns the output but ensures the exit value was as expected (0 by default) first
 * @throws IllegalStateException if exit value is not equal to expectedExitValue
 */
fun ExecResult.unwrap(expectedExitValue: Int = 0): List<String> =
    if (exitValue == expectedExitValue) stdOutAndErr
    else error("Exit value $exitValue is not equal to expected $expectedExitValue.")

fun ExecResult.check(expectedExitValue: Int = 0, expectedOutput: List<String>? = null) {
    val actualOutput = unwrap(expectedExitValue) // makes sure we first check exit value
    check(expectedOutput == null || expectedOutput == actualOutput)
}

