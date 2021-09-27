package pl.mareklangiewicz.kommand

import java.io.File

private fun Kommand.execStart(dir: String? = null): Process = ProcessBuilder()
    .command(listOf(name) + args)
    .directory(dir?.let(::File))
    .redirectErrorStream(true)
    .start()

private fun Kommand.execBlocking(dir: String? = null): ExecResult {
    val process = execStart(dir)
    val output = process.inputStream.bufferedReader().use { it.readLines() }
    val exit = process.waitFor()
    return ExecResult(exit, output)
}

/**
 * Execute given command (with optional args) in separate subprocess. Does not wait for it to end.
 * (the command should not expect any input or give any output or error)
 */
actual fun Kommand.exec(dir: String?) = execStart(dir).unit

/**
 * Runs given command in bash shell;
 * captures all its output (with error output merged in);
 * waits for the subprocess to finish;
 */
actual fun Kommand.shell(dir: String?) = kommand("bash", "-c", line()).execBlocking(dir)
// FIXME: create Kommand for bash and use it here too

