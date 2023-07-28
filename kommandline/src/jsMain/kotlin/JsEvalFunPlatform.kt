package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

actual typealias SysPlatform = JsEvalFunPlatform

class JsEvalFunPlatform: CliPlatform {

    override val isRedirectFileSupported get() = false

    private val debug = false

    @DelicateKommandApi
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
    ): ExecProcess {
        require(dir == null) { "dir unsupported" }
        require(inFile == null) { "inFile unsupported" }
        require(outFile == null) { "outFile unsupported" }
        require(errFile == null) { "errFile unsupported" }
        require(envModify == null) { "envModify unsupported" }
        val code = kommand.lineFun()
        if (debug) println(code)
        return JsEvalFunProcess(code)
    }

    companion object {
        fun enableDangerousJsEvalFun() { isEvalEnabled = true }
        fun disableDangerousJsEvalFun() { isEvalEnabled = false }
    }
}

private var isEvalEnabled = false

private class JsEvalFunProcess(code: String): ExecProcess {

    private var exit: Int
    private var out: Iterator<String>?
    private var err: Iterator<String>?

    var logln: (line: String) -> Unit = { console.log(it) }

    init {
        check(isEvalEnabled) { "eval is disabled"}
        try {
            exit = 0
            out = eval(code).toString().lines().iterator()
            err = null
        }
        catch (e: Exception) {
            exit = e::class.hashCode().mod(120) + 4 // positive number dependent on exception class
            out = null
            err = e.toString().lines().iterator()
        }
    }

    @DelicateKommandApi
    override fun waitForExit(finallyClose: Boolean): Int =
        try { exit }
        finally { if (finallyClose) close() }

    @OptIn(DelicateKommandApi::class)
    override suspend fun awaitExit(finallyClose: Boolean): Int = waitForExit(finallyClose)

    override fun kill(forcibly: Boolean) = error("cancel unsupported")

    @OptIn(DelicateKommandApi::class)
    override fun close() {
        stdinClose()
        stdoutClose()
        stderrClose()
    }

    @DelicateKommandApi
    override fun stdinWriteLine(line: String, lineEnd: String, thenFlush: Boolean) = logln(line)

    @DelicateKommandApi
    override fun stdinClose() { logln = {} }

    @DelicateKommandApi
    override fun stdoutReadLine(): String? = out?.takeIf { it.hasNext() }?.next()

    @DelicateKommandApi
    override fun stdoutClose() { out = null }

    @DelicateKommandApi
    override fun stderrReadLine(): String? = err?.takeIf { it.hasNext() }?.next()

    @DelicateKommandApi
    override fun stderrClose() { err = null }

    @OptIn(DelicateKommandApi::class)
    override suspend fun stdin(
        lineS: Flow<String>,
        lineEnd: String,
        flushAfterEachLine: Boolean,
        finallyStdinClose: Boolean,
    ) =
        try { lineS.collect { stdinWriteLine(it, lineEnd, flushAfterEachLine) } }
        finally { if (finallyStdinClose) stdinClose() }

    @OptIn(DelicateKommandApi::class)
    override val stdout: Flow<String> = stdFlow(::stdoutReadLine, ::stdoutClose)

    @OptIn(DelicateKommandApi::class)
    override val stderr: Flow<String> = stdFlow(::stderrReadLine, ::stderrClose)
}

private fun stdFlow(readLine: () -> String?, close: () -> Unit): Flow<String> =
    flow { while (true) emit(readLine() ?: break) }.onCompletion { close() }
