package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.*
import java.io.*
import java.lang.ProcessBuilder.*
import kotlin.coroutines.*

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

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
private fun safeDispatcher(name: String): CoroutineDispatcher =
    newSingleThreadContext(name)

private class JvmExecProcess(private val process: Process) : ExecProcess {

    // TODO_someday: analyze CAREFULLY if instead of newSingleThreadContext it's safe to use Dispatchers.IO.limitedParallelism(1)
    private val processDispatcher = safeDispatcher("JvmExecProcess.processDispatcher")
    private val stdinDispatcher = safeDispatcher("JvmExecProcess.stdinDispatcher")
    private val stdoutDispatcher = safeDispatcher("JvmExecProcess.stdoutDispatcher")
    private val stderrDispatcher = safeDispatcher("JvmExecProcess.stderrDispatcher")

    private val stdinWriter = process.outputWriter()
    private val stdoutReader = process.inputReader()
    private val stderrReader = process.errorReader()

    @DelicateKommandApi
    override fun waitForExit(finallyClose: Boolean) =
        try { process.waitFor() }
        finally { if (finallyClose) close() }

    override suspend fun awaitExit(finallyClose: Boolean): Int = withContext(processDispatcher) {
        try { process.onExit().await().exitValue() }
        finally { if (finallyClose) close() }
    }

    override fun kill(forcibly: Boolean) = processDispatcher.dispatch(EmptyCoroutineContext) {
        if (forcibly) process.destroyForcibly() else process.destroy()
    }

    @OptIn(DelicateKommandApi::class)
    override fun close() {
        stdinDispatcher.dispatch(EmptyCoroutineContext) { stdinClose() }
        stdoutDispatcher.dispatch(EmptyCoroutineContext) { stdoutClose() }
        stderrDispatcher.dispatch(EmptyCoroutineContext) { stderrClose() }
        // TODO_someday: analyze CAREFULLY if it would be safe here
        // to somehow schedule closing stdxxxDispatchers (release threads) AFTER not used anymore
        // with process.onExit with additional delay or sth??
    }

    @DelicateKommandApi
    override fun stdinWriteLine(line: String, thenFlush: Boolean) =
        stdinWriter.run { write(line); newLine(); if (thenFlush) flush() }

    @DelicateKommandApi
    override fun stdinClose() = stdinWriter.close()

    @DelicateKommandApi
    override fun stdoutReadLine(): String? = stdoutReader.readLine()

    @DelicateKommandApi
    override fun stdoutClose() = stdoutReader.close()

    @DelicateKommandApi
    override fun stderrReadLine(): String? = stderrReader.readLine()

    @DelicateKommandApi
    override fun stderrClose() = stderrReader.close()

    @OptIn(DelicateKommandApi::class)
    override val stdin: FlowCollector<String> = FlowCollector {
        withContext(stdinDispatcher) { stdinWriteLine(it) }
    }

    @OptIn(DelicateKommandApi::class)
    override val stdout: Flow<String> = stdFlow(::stdoutReadLine, ::stdoutClose, stdoutDispatcher)

    @OptIn(DelicateKommandApi::class)
    override val stderr: Flow<String> = stdFlow(::stderrReadLine, ::stderrClose, stderrDispatcher)
}

private fun stdFlow(readLine: () -> String?, close: () -> Unit, dispatcher: CoroutineDispatcher): Flow<String> =
    flow { while (true) emit(readLine() ?: break) }
        .onCompletion { close() }
        .flowOn(dispatcher)
