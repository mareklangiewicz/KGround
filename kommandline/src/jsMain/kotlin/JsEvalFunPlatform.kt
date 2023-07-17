package pl.mareklangiewicz.kommand

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

@DelicateKommandApi
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

    override fun waitForExit(thenCloseAll: Boolean): Int = exit
        .also { if (thenCloseAll) { stdinClose(); stdoutClose(); stderrClose() } }

    override fun cancel(force: Boolean) = error("cancel unsupported")

    override fun stdinWriteLine(line: String, thenFlush: Boolean) = logln(line)

    override fun stdinClose() { logln = {} }

    override fun stdoutReadLine(): String? = out?.takeIf { it.hasNext() }?.next()

    override fun stdoutClose() { out = null }

    override fun stderrReadLine(): String? = err?.takeIf { it.hasNext() }?.next()

    override fun stderrClose() { err = null }
}

