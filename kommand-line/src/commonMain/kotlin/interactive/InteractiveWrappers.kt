package pl.mareklangiewicz.interactive

import kotlinx.coroutines.CoroutineScope
import okio.Path
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.shell.*
import pl.mareklangiewicz.kommand.vim.*
import pl.mareklangiewicz.kommand.term.*
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.usubmit.xd.*

val isJvm: Boolean = getSysPlatformType()?.startsWith("JVM") == true

@DelicateApi("API for manual interactive experimentation.")
suspend fun isInteractiveCodeEnabled(): Boolean {
  val cli = localCLI()
  val log = localULog()
  return when {
    !isJvm -> false.also { log.w("Interactive code is only available on Jvm (for now).") }
    !getUserFlag(cli, "code.interactive") -> false.also { log.w("Interactive code NOT enabled.") }
    else -> true
  }
}

@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
suspend inline fun ifInteractiveCodeEnabled(code: suspend () -> Unit) {
  if (isInteractiveCodeEnabled()) code()
}

@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun ifInteractiveCodeEnabledBlockingOrErr(code: suspend () -> Unit) = runBlockingWithCLIAndULogOnJvmOnly {
  if (isInteractiveCodeEnabled()) code()
}

@OptIn(ExperimentalApi::class)
@DelicateApi("API for manual interactive experimentation; can ignore the this kommand.")
suspend fun Kommand.axInteractiveTry(
  confirmation: String = "Start ::${line()}::?",
  insideBash: Boolean = this !is TermKommand,
  insideTerm: Boolean = this !is TermKommand,
  pauseBeforeExit: Boolean = insideBash,
  optTermWrap: (innerKommand: Kommand) -> Kommand = { termXDefault(it) },
) = ifInteractiveCodeEnabled {
  val submit = localUSubmit()
  if (submit.askIf(confirmation)) {
    var kommand = when {
      insideBash -> inBash(pauseBeforeExit)
      pauseBeforeExit -> bad { "Can not pause before exit if not using bash shell" }
      else -> this
    }
    if (insideTerm) kommand = optTermWrap(kommand)
    kommand.ax()
  }
}


@Suppress("FunctionName")
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
inline fun <ReducedOut> InteractiveScript(crossinline ax: suspend () -> ReducedOut): ReducedScript<Unit> =
  ReducedScript { ifInteractiveCodeEnabled { ax() } }

// FIXME NOW: I want more InteractiveScripts in Samples instead of "tests" with weird logic when to skip them
//   rethink this
@DelicateApi("API for manual interactive experimentation. Conditionally skips.")
@Deprecated("Better to use Samples with InteractiveScript s")
suspend fun Kommand.tryInteractivelyCheck(expectedLineRaw: String? = null) {
  if (isJvm) toInteractiveCheck(expectedLineRaw).ax()
  // ifology just to avoid NotImplementedError on nonjvm. this extension fun will be deleted anyway (execb too)
}

@NotPortableApi
@DelicateApi
@Deprecated("Better to use Samples with InteractiveScript s")
fun Kommand.tryInteractivelyCheckBlockingOrErr(expectedLineRaw: String? = null) {
  runBlockingWithCLIAndULogOnJvmOnly {
    tryInteractivelyCheck(expectedLineRaw)
  }
}

@OptIn(DelicateApi::class, NotPortableApi::class)
internal fun runBlockingWithCLIAndULogOnJvmOnly(
  cli: CLI = getSysCLI(),
  log: ULog = ULogPrintLn(),
  block: suspend CoroutineScope.() -> Unit,
) {
  if (!isJvm) { println("Disabled on CLIs other than JVM."); return }
  runBlockingOrErr { uctx(cli + log) { block() } }
}


@DelicateApi("API for manual interactive experimentation. Conditionally skips")
fun Kommand.toInteractiveCheck(expectedLineRaw: String? = null): ReducedScript<Unit> =
  InteractiveScript {
    localULog().i(lineRaw())
    if (expectedLineRaw != null) lineRaw() chkEq expectedLineRaw
    axInteractiveTry()
  }


@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inLines: List<String>, vararg useNamedArgs: Unit, filePath: Path? = null) =
  InteractiveScript {
    val fs = localUFileSys()
    val cli = localCLI()
    val fp = filePath ?: fs.pathToTmpNotes
    writeFileWithDD(inLines, fp).ax()
    cli.lx(gvim(fp))
  }

@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inContent: String, vararg useNamedArgs: Unit, filePath: Path? = null) =
  writeFileAndStartInGVim(listOf(inContent), filePath = filePath)
