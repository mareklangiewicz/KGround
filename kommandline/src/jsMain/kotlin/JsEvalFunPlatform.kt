package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog

actual fun getSysCLI(): CLI = JsEvalFunCLI()

class JsEvalFunCLI(val log: ULog = UHackySharedFlowLog()) : CLI {

  override val isRedirectFileSupported get() = false

  @DelicateApi
  override fun lx(
    kommand: Kommand,
    vararg useNamedArgs: Unit,
    workDir: Path?,
    inFile: Path?,
    outFile: Path?,
    outFileAppend: Boolean,
    errToOut: Boolean,
    errFile: Path?,
    errFileAppend: Boolean,
    envModify: (MutableMap<String, String>.() -> Unit)?,
  ): ExecProcess {
    req(workDir == null) { "workDir unsupported" }
    req(inFile == null) { "inFile unsupported" }
    req(outFile == null) { "outFile unsupported" }
    req(errFile == null) { "errFile unsupported" }
    req(envModify == null) { "envModify unsupported" }
    val code = kommand.lineFun()
    log.d(code)
    return JsEvalFunProcess(code, log)
  }

  companion object {
    fun enableDangerousJsEvalFun() {
      isEvalEnabled = true
    }

    fun disableDangerousJsEvalFun() {
      isEvalEnabled = false
    }
  }
}

private var isEvalEnabled = false

private class JsEvalFunProcess(code: String, var log: ULog? = null) : ExecProcess {

  private var exit: Int
  private var out: Iterator<String>?
  private var err: Iterator<String>?

  init {
    chk(isEvalEnabled) { "eval is disabled" }
    try {
      exit = 0
      out = eval(code).strf.lines().iterator()
      err = null
    } catch (e: Exception) {
      exit = e::class.hashCode().mod(120) + 4 // positive number dependent on exception class
      out = null
      err = e.strf.lines().iterator()
    }
  }

  @DelicateApi
  override fun waitForExit(finallyClose: Boolean): Int =
    try {
      exit
    } finally {
      if (finallyClose) close()
    }

  @OptIn(DelicateApi::class)
  override suspend fun awaitExit(finallyClose: Boolean): Int = waitForExit(finallyClose)

  override fun kill(forcibly: Boolean) = bad { "cancel unsupported" }

  @OptIn(DelicateApi::class)
  override fun close() {
    stdinClose()
    stdoutClose()
    stderrClose()
  }

  @DelicateApi override fun stdinWriteLine(line: String, lineEnd: String, thenFlush: Boolean) { log?.i(line) }

  @DelicateApi override fun stdinClose() { log = null }

  @DelicateApi override fun stdoutReadLine(): String? = out?.takeIf { it.hasNext() }?.next()

  @DelicateApi override fun stdoutClose() { out = null }

  @DelicateApi override fun stderrReadLine(): String? = err?.takeIf { it.hasNext() }?.next()

  @DelicateApi override fun stderrClose() { err = null }

  @OptIn(DelicateApi::class)
  override val stdin = defaultStdinCollector(Dispatchers.Default, ::stdinWriteLine, ::stdinClose)

  @OptIn(DelicateApi::class)
  override val stdout: Flow<String> = defaultStdOutOrErrFlow(Dispatchers.Default, ::stdoutReadLine, ::stdoutClose)

  @OptIn(DelicateApi::class)
  override val stderr: Flow<String> = defaultStdOutOrErrFlow(Dispatchers.Default, ::stderrReadLine, ::stderrClose)
}
