package pl.mareklangiewicz.kommand

import java.io.File

actual typealias SysPlatform = JvmPlatform

class JvmPlatform: Platform {
    override fun start(
        kommand: Kommand,
        dir: String?,
        inFile: String?,
        outFile: String?
    ): ExecProcess = JvmExecProcess(ProcessBuilder().apply {
        command(listOf(kommand.name) + kommand.args)
        directory(dir?.let(::File))
        redirectErrorStream(true)
        inFile?.let { redirectInput(File(it)) }
        outFile?.let { redirectOutput(File(it)) }
    }.start())

    override val isJvm get() = true
    override val isDesktop get() = xdgdesktop.isEmpty()
    override val isUbuntu get() = "ubuntu" in xdgdesktop
    override val isGnome get() = "GNOME" in xdgdesktop

    override val pathToUserHome: String? get() = System.getProperty("user.home")
    override val pathToUserTmp: String? get() = if (isUbuntu) "$pathToUserHome/tmp" else null

    private val xdgdesktop by lazy { bashGetExports()["XDG_CURRENT_DESKTOP"]?.split(":").orEmpty() }
}

private class JvmExecProcess(private val process: Process): ExecProcess {
    override fun await(): ExecResult {
        val output = process.inputStream.bufferedReader().use { it.readLines() }
        val exit = process.waitFor()
        return ExecResult(exit, output)
    }
}