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
    override fun isDesktop() = bashGetExports()["XDG_CURRENT_DESKTOP"] != null
    override fun isUbuntu() = bashGetExports()["XDG_CURRENT_DESKTOP"]?.contains("ubuntu", ignoreCase = true) ?: false
    override fun isGnome() = bashGetExports()["XDG_CURRENT_DESKTOP"]?.contains("gnome", ignoreCase = true) ?: false

}
