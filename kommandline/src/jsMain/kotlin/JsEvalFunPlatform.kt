package pl.mareklangiewicz.kommand

actual typealias SysPlatform = JsEvalFunPlatform

class JsEvalFunPlatform: CliPlatform {

    override val isRedirectFileSupported get() = false

    private val debug = false

    @OptIn(DelicateKommandApi::class)
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

    var exit: Int
    var out: Sequence<String>
    var err: Sequence<String>

    init {
        check(isEvalEnabled) { "eval is disabled"}
        try {
            exit = 0
            out = eval(code).toString().lineSequence()
            err = emptySequence()
        }
        catch (e: Exception) {
            exit = e::class.hashCode().mod(120) + 4 // positive number dependent on exception class
            out = emptySequence()
            err = e.toString().lineSequence()
        }
    }

    override fun waitForExit() = exit

    override fun cancel(force: Boolean) = error("cancel unsupported")

    override fun useInLines(input: Sequence<String>):Unit = error("JsEvalFunProcess:useInputLines unsupported")

    override fun useOutLines(block: (output: Sequence<String>) -> Unit) = block(out)

    override fun useErrLines(block: (error: Sequence<String>) -> Unit) = block(err)
}

