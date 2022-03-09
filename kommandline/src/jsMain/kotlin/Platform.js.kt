package pl.mareklangiewicz.kommand

actual typealias SysPlatform = JsEvalFunPlatform

class JsEvalFunPlatform: Platform {

    private val debug = false

    override fun start(kommand: Kommand, dir: String?, inFile: String?, outFile: String?): ExecProcess {
        check(dir == null) { "dir unsupported" }
        check(inFile == null) { "inFile unsupported" }
        check(outFile == null) { "outFile unsupported" }
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
    override fun await() = ExecResult(
        exitValue = 0,
        stdOutAndErr = when {
            isEvalEnabled -> eval(code).toString()
            else -> "eval is disabled"
        }.lines()
    )

    override fun cancel(force: Boolean) = error("cancel unsupported")
}

