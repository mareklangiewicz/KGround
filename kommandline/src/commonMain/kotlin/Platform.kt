package pl.mareklangiewicz.kommand

interface Platform {

    /**
     * TODO_later: experiment with wrapping some remote platform in sth like bash kommands,
     * so it supports redirect using remote bash operators like < > << >> or sth like that.
     */
    val isRedirectSupported: Boolean

    /**
     * @param dir working directory for started subprocess - null means inherit from current process
     * @param inFile - redirect std input from given file - null means do not redirect
     * @param outFile - redirect std output (std err too) to given file - null means do not redirect
     */
    fun start(
        kommand: Kommand,
        dir: String? = null,
        inFile: String? = null,
        outFile: String? = null
    ): ExecProcess
    // TODO_later: access to input/output/error streams (when not redirected) with Okio source/sink
    // TODO_later: support for outFile appending (java:ProcessBuilder.Redirect.appendTo)
    // TODO_someday: @CheckResult https://youtrack.jetbrains.com/issue/KT-12719

    operator fun Kommand.invoke(dir: String? = null, inFile: String? = null, outFile: String? = null) =
        start(this, dir, inFile, outFile).await().unwrap()

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

class FakePlatform: Platform {

    override val isRedirectSupported get() = true // not really, but it's all fake

    override fun start(kommand: Kommand, dir: String?, inFile: String?, outFile: String?): ExecProcess {
        println("start($kommand, $dir)")
        return object : ExecProcess {
            override fun await(): ExecResult {
                println("await()")
                return ExecResult(0, emptyList())
            }
            override fun cancel(force: Boolean) = println("cancel($force)")
        }
    }
}

expect class SysPlatform(): Platform




interface ExecProcess {
    fun await(): ExecResult
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

