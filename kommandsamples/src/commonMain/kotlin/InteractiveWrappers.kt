package pl.mareklangiewicz.kommand.samples

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kommand.CLI
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.lineRawOrNull
import pl.mareklangiewicz.kommand.toInteractiveCheck
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.ulog
import pl.mareklangiewicz.ulog.d

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Any?.tryInteractivelyAnything(cli: CLI = SYS) = when (this) {
    is Sample -> tryInteractivelyCheckSample(cli)
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
        val tmpNotesFile = SYS.pathToUserTmp + "/tmp.notes"
        val lines = if (this is Collection<*>) map { it.toString() } else toString().lines()
        writeFileWithDD(lines, tmpNotesFile).ax(cli)
        ideOpen(tmpNotesFile).ax(cli)
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

