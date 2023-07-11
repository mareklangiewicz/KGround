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
        envModify: (MutableMap<String, String>.() -> Unit)?,
    ): ExecProcess {
        require(dir == null) { "dir unsupported" }
        require(inFile == null) { "inFile unsupported" }
        require(outFile == null) { "outFile unsupported" }
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

private class JsEvalFunProcess(private val code: String): ExecProcess {

    val outputLines: Sequence<String>

    init {
        check(isEvalEnabled) { "eval is disabled"}
        outputLines = eval(code).toString().lineSequence()
    }

    override fun waitForExit() = 0

    override fun cancel(force: Boolean) = error("cancel unsupported")

    override fun useInputLines(input: Sequence<String>):Unit = error("JsEvalFunProcess:useInputLines unsupported")

    override fun useOutputLines(block: (output: Sequence<String>) -> Unit) = block(outputLines)
}

