package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kommand.CLI
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.XClipSelection
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.lineRawOrNull
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.pathToTmpNotes
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.ulog
import pl.mareklangiewicz.kommand.xclipOut
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.ureflect.getReflectCallOrNull


/**
 * @param reference Either "xclip", or reference in format like from IntelliJ:CopyReference action.
 *   For example, "pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop"
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun tryInteractivelySomethingRef(reference: String = "xclip") {
    ulog.d("tryInteractivelySomethingRef(\"$reference\")")
    val ref = if (reference == "xclip")
        xclipOut(XClipSelection.Clipboard).ax(SYS).singleOrNull() ?: bad { "Clipboard has to have code reference in single line." }
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
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun tryInteractivelyClassMember(className: String, memberName: String) {
    ulog.d("tryInteractivelyClassMember(\"$className\", \"$memberName\")")
    val call = getReflectCallOrNull(className, memberName) ?: return
    // Note: prepareCallFor fails early if member not found,
    // before we start to interact with the user,
    // but the code is never called without confirmation.
    ifInteractiveCodeEnabled {
        zenityAskIf("Call $className#$memberName ?").ax(SYS) || return
        val member: Any? = call()
        // Note: call() will either already "do the thing" (when the member is just a fun to call)
        //  or it will only get the property (like ReducedScript/Sample etc.) which will be tried (or not) later.
        member.tryInteractivelyAnything()
    }
}


@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Any?.tryInteractivelyAnything(cli: CLI = SYS) = when (this) {
    is Sample -> tryInteractivelyCheckSample(cli)
    is Kommand -> toInteractiveCheck().ax(cli)
    is ReducedSample<*> -> tryInteractivelyCheckReducedSample(cli) // Note: ReducedSample is also ReducedScript
    is ReducedScript<*> -> tryInteractivelyCheckReducedScript(cli = cli)
    else -> tryOpenDataInIDE(cli = cli)
}


@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Sample.tryInteractivelyCheckSample(cli: CLI = SYS) =
    kommand.toInteractiveCheck(expectedLineRaw).ax(cli)

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedSample<*>.tryInteractivelyCheckReducedSample(cli: CLI = SYS) {
    reducedKommand.lineRawOrNull() chkEq expectedLineRaw // so also if both are nulls it's treated as fine.
    tryInteractivelyCheckReducedScript("Exec ReducedSample ?", cli)
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedScript<*>.tryInteractivelyCheckReducedScript(
    question: String = "Exec ReducedScript ?", cli: CLI = SYS
) {
    zenityAskIf(question).ax(cli) || return
    val reducedOut = ax(cli)
    reducedOut.tryOpenDataInIDE("Open ReducedOut: ${reducedOut.about} in tmp.notes in IDE ?")
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
/** @param question null means default question */
suspend fun Any?.tryOpenDataInIDE(question: String? = null, cli: CLI = SYS) = when {
    this == null -> ulog.d("It is null. Nothing to open.")
    this is Unit -> ulog.d("It is Unit. Nothing to open.")
    this is String && isEmpty() -> ulog.d("It is empty string. Nothing to open.")
    this is Collection<*> && isEmpty() -> ulog.d("It is empty collection. Nothing to open.")
    !zenityAskIf(question ?: "Open $about in tmp.notes in IDE ?").ax(cli) -> ulog.d("Not opening.")
    else -> {
        val lines = if (this is Collection<*>) map { it.toString() } else toString().lines()
        writeFileWithDD(lines, cli.pathToTmpNotes).ax(cli)
        ideOpen(cli.pathToTmpNotes).ax(cli)
    }
}

private val Any?.about: String get() = when (this) {
    null -> "null"
    Unit -> "Unit"
    is Number -> this::class.simpleName + ":$this"
    is Collection<*> -> this::class.simpleName + "(size:$size)"
    is CharSequence -> this::class.simpleName + "(length:$length)"
    else -> this::class.simpleName ?: "???"
}

