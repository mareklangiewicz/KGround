package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.usubmit.xd.*


@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
@ExperimentalApi("Will be removed someday. Temporary solution for running some code parts fast. Like examples/samples.")
suspend fun tryInteractivelyCodeRefWithLogging(reference: String) {
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
