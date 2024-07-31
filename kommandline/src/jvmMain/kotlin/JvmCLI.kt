package pl.mareklangiewicz.kommand

import java.lang.ProcessBuilder.*
import kotlin.coroutines.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*

actual fun getSysCLI(): CLI = JvmCLI()

class JvmCLI : CLI {

  override val isRedirectFileSupported get() = true

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
  ): ExecProcess =
    JvmExecProcess(
      ProcessBuilder()
        .apply {
          command(kommand.toArgs())
          directory(workDir?.toFile())
          inFile?.toFile()?.let(::redirectInput)
          outFile ?: chk(!outFileAppend) { "No output file to append to" }
          outFile?.toFile()?.let {
            redirectOutput(if (outFileAppend) Redirect.appendTo(it) else Redirect.to(it))
          }
          redirectErrorStream(errToOut)
          errFile ?: chk(!errFileAppend) { "No error file to append to" }
          errFile?.toFile()?.let {
            redirectError(if (errFileAppend) Redirect.appendTo(it) else Redirect.to(it))
          }
          envModify?.let { environment().it() }
        }
        .start(),
    )
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
private fun sequentialContext(name: String): CoroutineContext =
// TODO_someday: analyze CAREFULLY if instead of newSingleThreadContext it's safe to use Dispatchers.IO.limitedParallelism(1)
// UPDATE: I convinced myself it is safe. There is always happens-before guarantee and only one thread at a time is used.
//    newSingleThreadContext(name)
  Dispatchers.IO.limitedParallelism(1) + CoroutineName(name)

private fun CoroutineContext.tryDispatch(block: () -> Unit) =
  (this[ContinuationInterceptor] as? CoroutineDispatcher)
    ?.dispatch(this, block)
    ?: bad { "No dispatcher in coroutine ${this[CoroutineName]?.name}" }

private class JvmExecProcess(private val process: Process) : ExecProcess {

  private val processContext = sequentialContext("JvmExecProcess.processDispatcher")
  private val stdinContext = sequentialContext("JvmExecProcess.stdinDispatcher")
  private val stdoutContext = sequentialContext("JvmExecProcess.stdoutDispatcher")
  private val stderrContext = sequentialContext("JvmExecProcess.stderrDispatcher")

  private val stdinWriter = process.outputWriter()
  private val stdoutReader = process.inputReader()
  private val stderrReader = process.errorReader()

  @DelicateApi
  override fun waitForExit(finallyClose: Boolean) =
    try {
      process.waitFor()
    } finally {
      if (finallyClose) close()
    }

  override suspend fun awaitExit(finallyClose: Boolean): Int = withContext(processContext) {
    try {
      process.onExit().await().exitValue()
    } finally {
      if (finallyClose) close()
    }
  }

  override fun kill(forcibly: Boolean) = processContext.tryDispatch {
    if (forcibly) process.destroyForcibly() else process.destroy()
  }

  @OptIn(DelicateApi::class)
  override fun close() {
    stdinContext.tryDispatch { stdinClose() }
    stdoutContext.tryDispatch { stdoutClose() }
    stderrContext.tryDispatch { stderrClose() }
  }

  @DelicateApi
  override fun stdinWriteLine(line: String, lineEnd: String, thenFlush: Boolean) = stdinWriter.run {
    write(line)
    if (lineEnd.isNotEmpty()) write(lineEnd)
    if (thenFlush) flush()
  }

  @DelicateApi
  override fun stdinClose() = stdinWriter.close()

  @DelicateApi
  override fun stdoutReadLine(): String? = stdoutReader.readLine()

  @DelicateApi
  override fun stdoutClose() = stdoutReader.close()

  @DelicateApi
  override fun stderrReadLine(): String? = stderrReader.readLine()

  @DelicateApi
  override fun stderrClose() = stderrReader.close()

  @OptIn(DelicateApi::class)
  override val stdin = defaultStdinCollector(stdinContext, ::stdinWriteLine, ::stdinClose)

  @OptIn(DelicateApi::class)
  override val stdout: Flow<String> = defaultStdOutOrErrFlow(stdoutContext, ::stdoutReadLine, ::stdoutClose)

  @OptIn(DelicateApi::class)
  override val stderr: Flow<String> = defaultStdOutOrErrFlow(stderrContext, ::stderrReadLine, ::stderrClose)
}

