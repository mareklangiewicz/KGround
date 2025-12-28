@file:OptIn(ExperimentalApi::class)

package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.ide.ideOrGVimOpen
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.udata.LO
import pl.mareklangiewicz.udata.strfon
import pl.mareklangiewicz.ulog.*
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ureflect.*
import pl.mareklangiewicz.usubmit.*
import pl.mareklangiewicz.usubmit.xd.*


/**
 * @param reference Either "xclip",
 * or reference in format like from IntelliJ:CopyReference action,
 * for example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop",
 * or short format without class, like someMember,
 * (will try to match with a few known classes/objects),
 * but it always tries to run at most one (first) found code.
 * BTW also works for kotlin global functions/properties where class is synthetic "file facade",
 * f.e. "pl.mareklangiewicz.kommand.find.FindSamplesKt#getMyAbcdKPath"
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun tryInteractivelySomethingRef(reference: String) {
  val log = localULog()
  log.i("tryInteractivelySomethingRef(\"$reference\")")
  val ref = if (reference == "xclip")
    clipOut().ax().singleOrNull() ?: bad { "Clipboard has to have code reference in single line." }
  else reference
  var (className, methodName) = parseSomethingRef(ref)
  // BTW We will try to call only the first actually found code (className#methodName)
  if (className.isEmpty()) className = LO(
    "pl.mareklangiewicz.kgroundx.maintenance.MyBasicExamples",
    "pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples",
    "pl.mareklangiewicz.kgroundx.maintenance.MyOtherExamples",
    "pl.mareklangiewicz.kgroundx.maintenance.MyWeirdExamples",
    "pl.mareklangiewicz.kgroundx.workflows.MyWorkflowsExamples",
    "pl.mareklangiewicz.kgroundx.experiments.MyExperiments",
    "pl.mareklangiewicz.kgroundx.experiments.MyGitIgnored",
    // "pl.mareklangiewicz.kommand.demo.MyDemoSamples",
    // BTW not adding any different kommand samples, etc., user can use full "path" format for all that.
  ).firstOrNull { getReflectCallOrNull(it, methodName) != null }.reqNN { "The $methodName not found in any known class." }
  tryInteractivelyClassMember(className, methodName)
}

@NotPortableApi
private fun parseSomethingRef(ref: String): Pair<String, String> {
  if (ureIdent().matchEntireOrNull(ref) != null) return "" to ref // just methodName (empty class name)
  val ure = ure {
    +ure("className") {
      +chWordFirst
      1..MAX of chWordOrDot
      +chWord
    }
    +ch('#')
    +ureIdent().withName("methodName")
  }
  val result = ure.matchEntireOrNull(ref) ?: bad { "This ref doesn't match method reference pattern" }
  val className by result.namedValues
  val methodName by result.namedValues
  return className!! to methodName!!
}

/**
 * BTW also works for kotlin global functions where className is synthetic "file facade" class,
 * f.e. "pl.mareklangiewicz.kommand.find.FindSamplesKt"
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
  val log = localULog()
  log.i("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
  val call = getReflectCallOrNull(className, memberName) ?: return
  // BTW it returns early if member not found, before we start to interact with the user,
  // but the invariant holds: the actual code is never run without user confirmation.
  ifInteractiveCodeEnabled {
    val submit = localUSubmit()
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
  is Kommand -> tryInteractivelyCheck()
  is ReducedSample<*> -> tryInteractivelyCheckReducedSample() // Note: ReducedSample is also ReducedScript
  is ReducedScript<*> -> tryInteractivelyCheckReducedScript()
  else -> tryOpenDataInIDEOrGVim()
}


@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun Sample.tryInteractivelyCheckSample() = kommand.tryInteractivelyCheck(expectedLineRaw)
// BTW name suffix because conflict: Sample is also Kommand

@DelicateApi("API for manual interactive experimentation. Conditionally skips.")
suspend fun Kommand.tryInteractivelyCheck(expectedLineRaw: String? = null) = toInteractiveScript(expectedLineRaw).ax()


@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun ReducedSample<*>.tryInteractivelyCheckReducedSample() {
  reducedKommand.lineRawOrNull() chkEq expectedLineRaw // so also if both are nulls it's treated as fine.
  tryInteractivelyCheckReducedScript("Exec ReducedSample ?")
}

@DelicateApi("API for manual interactive experimentation. Conditionally skips")
suspend fun ReducedScript<*>.tryInteractivelyCheckReducedScript(
  question: String = "Exec ReducedScript ?",
) {
  val submit = localUSubmit()
  submit.askIf(question) || return
  val reducedOut = ax()
  reducedOut.tryOpenDataInIDEOrGVim("Open ReducedOut: ${reducedOut.about} in tmp.notes in IDE (if running) or in GVim ?")
}

@DelicateApi("API for manual interactive experimentation. Conditionally skips")
/** @param question null means default question */
suspend fun Any?.tryOpenDataInIDEOrGVim(question: String? = null): Any {
  val log = localULog()
  val fs = localUFileSys()
  val submit = localUSubmit()
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
      val lines = if (this is Collection<*>) map { it.strfon } else strf.lines()
      val notes = fs.pathToTmpNotes
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
