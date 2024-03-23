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
    reducedOut.tryOpenDataInIDE("Open ReducedOut in tmp.notes in IDE ?")
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Any?.tryOpenDataInIDE(question: String = "Open this data in tmp.notes in IDE ?", cli: CLI = SYS) = when {
    this == null -> println("It is null. Nothing to open.")
    this is Unit -> println("It is Unit. Nothing to open.")
    this is String && isEmpty() -> println("It is empty string. Nothing to open.")
    this is Collection<*> && isEmpty() -> println("It is empty collection. Nothing to open.")
    !zenityAskIf(question).ax(cli) -> println("Not opening.")
    else -> {
        val tmpNotesFile = SYS.pathToUserTmp + "/tmp.notes"
        writeFileWithDD(listOf(toString()), tmpNotesFile).ax(cli)
        ideOpen(tmpNotesFile).ax(cli)
    }
}

