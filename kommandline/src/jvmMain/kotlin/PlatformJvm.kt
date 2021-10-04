package pl.mareklangiewicz.kommand

import java.io.File

actual typealias SysPlatform = JvmPlatform

class JvmPlatform: Platform {
    override fun execStart(kommand: Kommand, dir: String?): ExecProcess {
        val process: Process = ProcessBuilder()
            .command(listOf(kommand.name) + kommand.args)
            .directory(dir?.let(::File))
            .redirectErrorStream(true)
            .start()
        return ExecProcess {
            val output = process.inputStream.bufferedReader().use { it.readLines() }
            val exit = process.waitFor()
            ExecResult(exit, output)
        }
    }
    override fun isJvm() = true
    override fun isDesktop() = xdgdesktop.isEmpty()
    override fun isUbuntu() = "ubuntu" in xdgdesktop
    override fun isGnome() = "GNOME" in xdgdesktop
    private val xdgdesktop by lazy { bashGetExports()["XDG_CURRENT_DESKTOP"]?.split(":").orEmpty() }
}
