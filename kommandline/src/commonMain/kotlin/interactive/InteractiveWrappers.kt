package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.term.termXDefault
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.w

// TODO: Make these interactive wrappers suspendable (or delete if when needed at all) and use: implictx<ULog>()
private val log: ULog = UHackySharedFlowLog()

@DelicateApi("API for manual interactive experimentation.")
suspend fun isInteractiveCodeEnabled(): Boolean {
  val cli = implictx<CLI>()
  return when {
    !cli.isJvm -> false.also { log.w("Interactive code is only available on JvmCLI (for now).") }
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
fun ifInteractiveCodeEnabledBlockingOrErr(code: suspend () -> Unit) = runBlockingOrErr {
  uctx(provideSysCLI()) {
    if (isInteractiveCodeEnabled()) code()
  }
}

@DelicateApi("API for manual interactive experimentation; requires zenity; can ignore the this kommand.")
suspend fun Kommand.tryInteractivelyStartInTerm(
  confirmation: String = "Start ::${line()}:: in terminal?",
  title: String = name,
  insideBash: Boolean = true,
  pauseBeforeExit: Boolean = insideBash,
  startInDir: String? = null,
  termKommand: (innerKommand: Kommand) -> Kommand = { termXDefault(it) },
) = ifInteractiveCodeEnabled {
  if (zenityAskIf(confirmation, title).ax()) {
    val k = when {
      insideBash -> bash(this, pauseBeforeExit)
      pauseBeforeExit -> bad { "Can not pause before exit if not using bash shell" }
      else -> this
    }
    val cli = implictx<CLI>()
    cli.start(termKommand(k), dir = startInDir)
  }
}


@Suppress("FunctionName")
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
inline fun <ReducedOut> InteractiveScript(crossinline ax: suspend () -> ReducedOut) =
  ReducedScript { ifInteractiveCodeEnabled { ax() } }

// FIXME NOW: I want more InteractiveScripts in Samples instead of "tests" with weird logic when to skip them
//   rethink this
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
@Deprecated("Better to use Samples with InteractiveScript s")
suspend fun Kommand.tryInteractivelyCheck(expectedLineRaw: String? = null, execInDir: String? = null) {
  val cli = implictx<CLI>()
  if (cli.isJvm) toInteractiveCheck(expectedLineRaw, execInDir).ax()
  // ifology just to avoid NotImplementedError on nonjvm. this extension fun will be deleted anyway (execb too)
}

@NotPortableApi
@DelicateApi
fun Kommand.tryInteractivelyCheckBlockingOrErr(expectedLineRaw: String? = null, execInDir: String? = null) {
  runBlockingOrErr {
    uctx(provideSysCLI()) {
      tryInteractivelyCheck(expectedLineRaw, execInDir)
    }
  }
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
fun Kommand.toInteractiveCheck(expectedLineRaw: String? = null, execInDir: String? = null) =
  InteractiveScript {
    log.d(lineRaw())
    if (expectedLineRaw != null) lineRaw() chkEq expectedLineRaw
    tryInteractivelyStartInTerm(startInDir = execInDir)
  }


@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inLines: List<String>, vararg useNamedArgs: Unit, filePath: String? = null) =
  InteractiveScript {
    val cli = implictx<CLI>()
    val fp = filePath ?: cli.pathToTmpNotes
    writeFileWithDD(inLines, fp).ax()
    cli.start(gvim(fp))
  }

@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inContent: String, vararg useNamedArgs: Unit, filePath: String? = null) =
  writeFileAndStartInGVim(listOf(inContent), filePath = filePath)

