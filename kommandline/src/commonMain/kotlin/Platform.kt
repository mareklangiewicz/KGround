package pl.mareklangiewicz.kommand

interface Platform {

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

    val isJvm: Boolean get() = false
    val isDesktop: Boolean get() = false
    val isUbuntu: Boolean get() = false
    val isGnome: Boolean get() = false

    val pathToUserHome get (): String? = null
    val pathToUserTmp get (): String? = null

    // TODO_someday: access to input/output streams wrapped in okio Source/Sink
    // (but what about platforms running kommands through ssh or adb?)

    companion object {
        val SYS = SysPlatform()
        val FAKE = FakePlatform()
    }
}

class FakePlatform: Platform {
    override fun start(kommand: Kommand, dir: String?, inFile: String?, outFile: String?): ExecProcess {
        println("execStart($kommand, $dir)")
        return object : ExecProcess {
            override fun await(): ExecResult {
                println("await()")
                return ExecResult(0, emptyList())
            }
        }
    }
}

expect class SysPlatform(): Platform




interface ExecProcess {
    fun await(): ExecResult
}

data class ExecResult(val exitValue: Int, val stdOutAndErr: List<String>)

/**
 * Returns the output but ensures the exit value was 0 first
 * @throws IllegalStateException if exit value is not equal to expectedExitValue
 */
fun ExecResult.unwrap(expectedExitValue: Int = 0): List<String> =
    if (exitValue == expectedExitValue) stdOutAndErr
    else throw IllegalStateException("Exit value $exitValue is not equal to expected $expectedExitValue.")

fun ExecResult.check(expectedExitValue: Int = 0, expectedOutput: List<String>? = null) {
    check(exitValue == expectedExitValue)
    expectedOutput?.let { check(stdOutAndErr == it) }
}

