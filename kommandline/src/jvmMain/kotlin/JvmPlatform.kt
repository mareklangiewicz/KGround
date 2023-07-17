package pl.mareklangiewicz.kommand

import java.io.*
import java.lang.ProcessBuilder.*

actual typealias SysPlatform = JvmPlatform

class JvmPlatform : CliPlatform {

    override val isRedirectFileSupported get() = true

    private val debug = false

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
        envModify: (MutableMap<String, String>.() -> Unit)?,
    ): ExecProcess =
        JvmExecProcess(
            ProcessBuilder()
                .apply {
                    if (debug) println(kommand.line())
                    command(kommand.toArgs())
                    directory(dir?.let(::File))
                    inFile?.let(::File)?.let(::redirectInput)
                    outFile ?: check(!outFileAppend) { "No output file to append to" }
                    outFile?.let(::File)?.let {
                        redirectOutput(if (outFileAppend) Redirect.appendTo(it) else Redirect.to(it))
                    }
                    redirectErrorStream(errToOut)
                    errFile ?: check(!errFileAppend) { "No error file to append to" }
                    errFile?.let(::File)?.let {
                        redirectError(if (errFileAppend) Redirect.appendTo(it) else Redirect.to(it))
                    }
                    envModify?.let { environment().it() }
                }
                .start()
        )

    override val isJvm get() = true
    override val isDesktop get() = xdgdesktop.isEmpty()
    override val isUbuntu get() = "ubuntu" in xdgdesktop
    override val isGnome get() = "GNOME" in xdgdesktop

    override val pathToUserHome: String? get() = System.getProperty("user.home")
    override val pathToUserTmp: String? get() = "$pathToUserHome/tmp" // FIXME_maybe: other paths for specific OSes? sometimes null?
    override val pathToSystemTmp: String? get() = System.getProperty("java.io.tmpdir")
    private val xdgdesktop by lazy { bashGetExportsExec()["XDG_CURRENT_DESKTOP"]?.split(":").orEmpty() }
}

@DelicateKommandApi
private class JvmExecProcess(private val process: Process) : ExecProcess {

    private val stdin = process.outputWriter()
    private val stdout = process.inputReader()
    private val stderr = process.errorReader()

    // TODO_someday: suspending version based on Process.onExit(): CompletableFuture
    // but: It looks like default onExit implementation just calls blocking: waitFor in a loop anyway in special thread.
    // so it's "thread expensive" anyway.. check what "onExit" implementation is actually used in my cases..
    // maybe they're planning to change onExit in the future to make it really nonblocking (NIO?)??
    override fun waitForExit(thenCloseAll: Boolean) = process.waitFor()
        .also { if (thenCloseAll) { stdinClose(); stdoutClose(); stderrClose() } }

    override fun cancel(force: Boolean) { if (force) process.destroyForcibly() else process.destroy() }

    override fun stdinWriteLine(line: String, thenFlush: Boolean) =
        stdin.run { write(line); newLine(); if (thenFlush) flush() }

    override fun stdinClose() = stdin.close()

    override fun stdoutReadLine(): String? = stdout.readLine()

    override fun stdoutClose() = stdout.close()

    override fun stderrReadLine(): String? = stderr.readLine()

    override fun stderrClose() = stderr.close()
}
