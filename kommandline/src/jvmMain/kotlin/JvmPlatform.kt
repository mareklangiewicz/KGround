package pl.mareklangiewicz.kommand

import java.io.File

actual typealias SysPlatform = JvmPlatform

class JvmPlatform: CliPlatform {

    override val isRedirectFileSupported get() = true
    override val isRedirectContentSupported get() = true

    private val debug = false

    override fun start(
        kommand: Kommand,
        vararg useNamedArgs: Unit,
        dir: String?,
        inFile: String?,
        outFile: String?,
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
    override fun await(inContent: String?): ExecResult {
        if (inContent != null) process.outputStream.bufferedWriter().use { it.write(inContent) }
        val output = process.inputStream.bufferedReader().use { it.readLines() }
        val exit = process.waitFor()
        return ExecResult(exit, output)
    }

    override fun cancel(force: Boolean) { if (force) process.destroyForcibly() else process.destroy() }
}
