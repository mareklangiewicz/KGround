package pl.mareklangiewicz.interactive

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.usubmit.xd.*

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this fun (called from main fun) allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.app.Playground#play
 * So way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandapp:run task is set up to run the mainCodeExperiments fun here.
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
@ExperimentalApi("Will be removed someday. Temporary solution for running some code parts fast. Like examples/samples.")
internal fun mainCodeExperiments(args: Array<String>) {
  val a0 = args.getOrNull(0).orEmpty()
  val a1 = args.getOrNull(1).orEmpty()
  val a2 = args.getOrNull(2).orEmpty()
  runBlockingMain(a0) {
    when {
      args.size == 2 && a0 == "try-code" -> tryInteractivelyCodeRefWithLogging(a1)
      // Note: get and set flag can't use interactive features, because it should work even if disabled.
      args.size == 2 && a0 == "get-user-flag" -> localULog().i(getUserFlagFullStr(localCLI(), a1))
      args.size == 3 && a0 == "set-user-flag" -> setUserFlag(localCLI(), a1, a2.toBoolean())
      else -> bad { "Incorrect args. See KommandLine -> InteractiveSamples.kt -> mainCodeExperiments" }
    }
  }
}

private fun runBlockingMain(name: String, block: suspend CoroutineScope.() -> Unit) =
  runBlocking {
    val log = UHackySharedFlowLog { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
    uctxWithIO(
      context = log + ZenitySupervisor() + getSysCLI(),
      name = name,
      // dispatcher = null, // FIXME_later: rethink default dispatcher
      block = block,
    )
  }

@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
@ExperimentalApi("Will be removed someday. Temporary solution for running some code parts fast. Like examples/samples.")
private suspend fun tryInteractivelyCodeRefWithLogging(reference: String) {
  val log = localULog()
  try {
    log.w("try-code $reference starting")
    withLogBadStreams {
      tryInteractivelySomethingRef(reference)
      log.w("try-code $reference finished")
    }
  } catch (ex: Exception) {
    log.w("try-code $reference failed")
    log.exWithTrace(ex)
  }
  tryInteractivelyOpenLogCache()
}

@OptIn(DelicateApi::class, ExperimentalApi::class) suspend fun tryInteractivelyOpenLogCache() {
  val lines = localULogAsOrNull<UHackySharedFlowLog>()?.flow?.replayCache ?: return
  val fs = localUFileSys()
  val submit = localUSubmit()
  val notes = fs.pathToTmpNotes
  isInteractiveCodeEnabled() && submit.askIf("Try to open log cache in IDE (in tmp.notes)?") || return
  writeFileWithDD(lines, notes).ax()
  ideOrGVimOpen(notes).ax()
}

// FIXME: common "UStr" utils with different MPP parametrized exception conversions / string representations.
//   (start with moving some of what is in UWidgets to KGround) (see comments in UHackyLog)
@Deprecated("This is temporary fast&dirty impl")
private fun ULog.exWithTrace(ex: Throwable) {
  e(ex.strf)
  ex.stackTraceToString().let {
    e("STACK TRACE:")
    it.lines().logEach(this, ULogLevel.ERROR)
    // each line have to be logged separately: don't use it.joinToString("\n"), because truncation in logger
  }
  ex.cause?.let {
    e("CAUSE:")
    exWithTrace(it)
  }
}
