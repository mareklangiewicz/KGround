package pl.mareklangiewicz.kommand

interface Platform {

    fun execStart(kommand: Kommand, dir: String? = null): ExecProcess

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
    override fun execStart(kommand: Kommand, dir: String?): ExecProcess {
        println("execStart($kommand, $dir)")
        return object : ExecProcess {
            override fun waitFor(): ExecResult {
                println("waitFor()")
                return ExecResult(0, emptyList())
            }
        }
    }
}

expect class SysPlatform(): Platform

fun Platform.execBlock(kommand: Kommand, dir: String? = null): ExecResult = execStart(kommand, dir).waitFor()

/**
 * Execute given command (with optional args) in separate subprocess. Does not wait for it to end.
 * (the command should not expect any input or give any output or error)
 */
fun Platform.exec(kommand: Kommand, dir: String? = null) = execStart(kommand, dir).unit


/**
 * Runs given command in bash shell;
 * captures all its output (with error output merged in);
 * waits for the subprocess to finish;
 */
fun Platform.shell(kommand: Kommand, dir: String? = null) = execBlock(bash(kommand), dir)


fun interface ExecProcess {
    fun waitFor(): ExecResult
}

data class ExecResult(val exitValue: Int, val stdOutAndErr: List<String>)

/**
 * Returns the output but ensures the exit value was 0 first
 * @throws IllegalStateException if exit value is not equal to expectedExitValue
 */
fun ExecResult.output(expectedExitValue: Int = 0): List<String> =
    if (exitValue == expectedExitValue) stdOutAndErr
    else throw IllegalStateException("Exit value $exitValue != expected $expectedExitValue.")


