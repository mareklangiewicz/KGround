package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.udata.*
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.hack.*
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.implictx
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ureflect.getReflectCallOrNull
import pl.mareklangiewicz.usubmit.USubmit
import pl.mareklangiewicz.usubmit.implictx
import pl.mareklangiewicz.usubmit.xd.askIf

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
@DelicateApi("API for manual interactive experimentation. Conditionally skips. Can easily call any code by reflection.")
@ExperimentalApi("Will be removed someday. Temporary solution for running some code parts fast. Like examples/samples.")
suspend fun mainCodeExperiments(args: Array<String>) {
  val log = UHackySharedFlowLog { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
  val submit = ZenitySupervisor()
  val cli = getSysCLI()
  val a0 = args.firstOrNull()
  // uctxWithIO(log + submit + cli, dispatcher = null) { // FIXME_later: rethink default dispatcher..
  uctxWithIO(log + submit + cli, name = a0) {
    when {
      args.size == 2 && a0 == "try-code" -> withLogBadStreams { tryInteractivelySomethingRef(args[1]) }
      args.size == 2 && a0 == "get-user-flag" -> log.i(getUserFlagFullStr(cli, args[1]))
      args.size == 3 && a0 == "set-user-flag" -> setUserFlag(cli, args[1], args[2].toBoolean())
      else -> bad { "Incorrect args. See KommandLine -> InteractiveSamples.kt -> mainCodeExperiments" }
    }
  }
}

/**
 * @param reference Either "xclip", or reference in format like from IntelliJ:CopyReference action.
 *   For example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop"
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun tryInteractivelySomethingRef(reference: String = "xclip") {
  val log = implictx<ULog>()
  log.i("tryInteractivelySomethingRef(\"$reference\")")
  val ref = if (reference == "xclip")
    xclipOut(XClipSelection.Clipboard).ax().singleOrNull()
      ?: bad { "Clipboard has to have code reference in single line." }
  else reference
  val ure = ure {
    +ure("className") {
      +chWordFirst
      1..MAX of chWordOrDot
      +chWord
    }
    +ch('#')
    +ureIdent().withName("methodName")
  }
  val result = ure.matchEntireOrThrow(ref)
  val className by result.namedValues
  val methodName by result.namedValues
  tryInteractivelyClassMember(className!!, methodName!!)
}

@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
  val log = implictx<ULog>()
  log.i("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
  val call = getReflectCallOrNull(className, memberName) ?: return
  // Note: prepareCallFor fails early if member not found,
  // before we start to interact with the user,
  // but the code is never called without confirmation.
  ifInteractiveCodeEnabled {
    val submit = implictx<USubmit>()
    submit.askIf("Call member $memberName\nfrom class $className?") || return
    val member: Any? = call()
    // Note: call() will either already "do the thing" (when the member is just a fun to call)
    //  or it will only get the property (like ReducedScript/Sample etc.) which will be tried (or not) later.
    member.tryInteractivelyAnything()
  }
}


@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun Any?.tryInteractivelyAnything() = when (this) {
  is Sample -> tryInteractivelyCheckSample()
  is Kommand -> toInteractiveCheck().ax()
  is ReducedSample<*> -> tryInteractivelyCheckReducedSample() // Note: ReducedSample is also ReducedScript
  is ReducedScript<*> -> tryInteractivelyCheckReducedScript()
  else -> tryOpenDataInIDEOrGVim()
}


@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun Sample.tryInteractivelyCheckSample() =
  kommand.toInteractiveCheck(expectedLineRaw).ax()

@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun ReducedSample<*>.tryInteractivelyCheckReducedSample() {
  reducedKommand.lineRawOrNull() chkEq expectedLineRaw // so also if both are nulls it's treated as fine.
  tryInteractivelyCheckReducedScript("Exec ReducedSample ?")
}

@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun ReducedScript<*>.tryInteractivelyCheckReducedScript(
  question: String = "Exec ReducedScript ?",
) {
  val submit = implictx<USubmit>()
  submit.askIf(question) || return
  val reducedOut = ax()
  reducedOut.tryOpenDataInIDEOrGVim("Open ReducedOut: ${reducedOut.about} in tmp.notes in IDE (if running) or in GVim ?")
}

@DelicateApi("API for manual interactive experimentation. Conditionally skips")
/** @param question null means default question */
suspend fun Any?.tryOpenDataInIDEOrGVim(question: String? = null): Any {
  val log = implictx<ULog>()
  val fs = implictx<UFileSys>()
  val submit = implictx<USubmit>()
  return when {
    this == null -> log.i("It is null. Nothing to open.")
    this is Unit -> log.i("It is Unit. Nothing to open.")
    this is Int && this in 0..10 -> log.i("It is small Int: $this. Nothing to open.")
    this is Long && this in 0..10 -> log.i("It is small Long: $this. Nothing to open.")
    this is Boolean -> log.i("It is Boolean: $this. Nothing to open.")
    this is String && isEmpty() -> log.i("It is empty string. Nothing to open.")
    this is Collection<*> && isEmpty() -> log.i("It is empty collection. Nothing to open.")
    !submit.askIf(question ?: "Open $about in tmp.notes in IDE (if running) or in GVim ?") -> log.i("Not opening.")
    else -> {
      val lines = if (this is Collection<*>) map { it.toString() } else toString().lines()
      val notes = fs.pathToTmpNotes.toString() // FIXME_later: use Path type everywhere
      writeFileWithDD(lines, notes).ax()
      ideOrGVimOpen(notes).ax()
    }
  }
}

private val Any?.about: String
  get() = when (this) {
    null -> "null"
    Unit -> "Unit"
    is Number -> this::class.simpleName + ":$this"
    is Collection<*> -> this::class.simpleName + "(size:$size)"
    is CharSequence ->
      if (length < 20) this::class.simpleName + ": \"$this\""
      else this::class.simpleName + "(length:$length)"
    else -> this::class.simpleName ?: "???"
  }

