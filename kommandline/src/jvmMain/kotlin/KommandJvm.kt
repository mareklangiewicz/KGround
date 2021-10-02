package pl.mareklangiewicz.kommand

import java.io.File

actual class ExecProcess actual constructor(kommand: Kommand, dir: String?) {

    private val process: Process = ProcessBuilder()
        .command(listOf(kommand.name) + kommand.args)
        .directory(dir?.let(::File))
        .redirectErrorStream(true)
        .start()

    actual fun waitFor(): ExecResult {
        val output = process.inputStream.bufferedReader().use { it.readLines() }
        val exit = process.waitFor()
        return ExecResult(exit, output)
    }
}
