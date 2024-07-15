package pl.mareklangiewicz.interactive

import kotlinx.coroutines.CoroutineScope
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.io.UFileSys
import pl.mareklangiewicz.kground.io.getSysPlatformType
import pl.mareklangiewicz.kground.io.implictx
import pl.mareklangiewicz.kground.io.pathToTmpNotes
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.term.termXDefault
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogPrintLn
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.implictx
import pl.mareklangiewicz.ulog.w
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.usubmit.implictx
import pl.mareklangiewicz.usubmit.xd.*

// TODO: Make these interactive wrappers suspendable (or delete if when needed at all) and use: implictx<ULog>()
private val log: ULog = UHackySharedFlowLog()

val isJvm: Boolean = getSysPlatformType()?.startsWith("JVM") == true

@DelicateApi("API for manual interactive experimentation.")
suspend fun isInteractiveCodeEnabled(): Boolean {
  val cli = implictx<CLI>()
  val log = implictx<ULog>()
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

@DelicateApi("API for manual interactive experimentation; can ignore the this kommand.")
suspend fun Kommand.tryInteractivelyStartInTerm(
  confirmation: String = "Start ::${line()}:: in terminal?",
  insideBash: Boolean = true,
  pauseBeforeExit: Boolean = insideBash,
  startInDir: String? = null,
  termKommand: (innerKommand: Kommand) -> Kommand = { termXDefault(it) },
) = ifInteractiveCodeEnabled {
  val submit = implictx<USubmit>()
  if (submit.askIf(confirmation)) {
    val k = when {
      insideBash -> bash(this, pauseBeforeExit)
      pauseBeforeExit -> bad { "Can not pause before exit if not using bash shell" }
      else -> this
    }
    val cli = implictx<CLI>()
    cli.lx(termKommand(k), dir = startInDir)
  }
}


@Suppress("FunctionName")
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
inline fun <ReducedOut> InteractiveScript(crossinline ax: suspend () -> ReducedOut) =
  ReducedScript { ifInteractiveCodeEnabled { ax() } }

// FIXME NOW: I want more InteractiveScripts in Samples instead of "tests" with weird logic when to skip them
//   rethink this
@DelicateApi("API for manual interactive experimentation. Conditionally skips.")
@Deprecated("Better to use Samples with InteractiveScript s")
suspend fun Kommand.tryInteractivelyCheck(expectedLineRaw: String? = null, execInDir: String? = null) {
  if (isJvm) toInteractiveCheck(expectedLineRaw, execInDir).ax()
  // ifology just to avoid NotImplementedError on nonjvm. this extension fun will be deleted anyway (execb too)
}

@NotPortableApi
@DelicateApi
fun Kommand.tryInteractivelyCheckBlockingOrErr(expectedLineRaw: String? = null, execInDir: String? = null) {
  runBlockingWithCLIAndULogOnJvmOnly {
    tryInteractivelyCheck(expectedLineRaw, execInDir)
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
fun Kommand.toInteractiveCheck(expectedLineRaw: String? = null, execInDir: String? = null) =
  InteractiveScript {
    log.d(lineRaw())
    if (expectedLineRaw != null) lineRaw() chkEq expectedLineRaw
    tryInteractivelyStartInTerm(startInDir = execInDir)
  }


@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inLines: List<String>, vararg useNamedArgs: Unit, filePath: String? = null) =
  InteractiveScript {
    val fs = implictx<UFileSys>()
    val cli = implictx<CLI>()
    val fp = filePath ?: fs.pathToTmpNotes.toString() // FIXME_later: Use Path everywhere
    writeFileWithDD(inLines, fp).ax()
    cli.lx(gvim(fp))
  }

@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inContent: String, vararg useNamedArgs: Unit, filePath: String? = null) =
  writeFileAndStartInGVim(listOf(inContent), filePath = filePath)

