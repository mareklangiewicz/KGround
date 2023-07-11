package pl.mareklangiewicz.kommand

import java.io.File

actual typealias SysPlatform = JvmPlatform

class JvmPlatform: CliPlatform {

    override val isRedirectFileSupported get() = true

    private val debug = false

    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?,
        //outFileAppend: Boolean, // TODO_someday_maybe
        envModify: (MutableMap<String, String>.() -> Unit)?,
    ): ExecProcess =
        JvmExecProcess(
            ProcessBuilder()
                .apply {
                    if (debug) println(kommand.line())
                    command(kommand.toArgs())
                    directory(dir?.let(::File))
                    redirectErrorStream(true)
                    inFile?.let { redirectInput(File(it)) }
                    outFile?.let { redirectOutput(File(it)) }
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

private class JvmExecProcess(private val process: Process): ExecProcess {

    // TODO_someday: suspending version based on Process.onExit(): CompletableFuture
    // but: It looks like default onExit implementation just calls blocking: waitFor in a loop anyway in special thread.
    // so it's "thread expensive" anyway.. check what "onExit" implementation is actually used in my cases..
    // maybe they're planning to change onExit in the future to make it really nonblocking (NIO?)??
    override fun waitForExit() = process.waitFor()

    override fun cancel(force: Boolean) { if (force) process.destroyForcibly() else process.destroy() }

    override fun useInputLines(input: Sequence<String>) = process.outputWriter().use {writer ->
        input.forEach {  writer.write(it); writer.newLine() }
    }

    override fun useOutputLines(block: (output: Sequence<String>) -> Unit) = process.inputReader().useLines(block)
}
