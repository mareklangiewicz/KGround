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
import pl.mareklangiewicz.kommand.exec

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun Sample.tryInteractivelyCheckSample(cli: CLI = SYS) =
    kommand.toInteractiveCheck(expectedLineRaw).exec(cli)

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedSample<*>.tryInteractivelyCheckReducedSample(cli: CLI = SYS) {
    reducedKommand.lineRawOrNull() chkEq expectedLineRaw // so also if both are nulls it's treated as fine.
    tryInteractivelyCheckReducedScript("Exec ReducedSample ?", cli)
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
suspend fun ReducedScript<*>.tryInteractivelyCheckReducedScript(
    question: String = "Exec ReducedScript ?", cli: CLI = SYS
) {
    zenityAskIf(question).exec(cli) || return
    val reducedOut = exec(cli)
    when (reducedOut) {
        null, Unit -> return
        is String -> if (reducedOut.isEmpty()) return
        is Collection<*> -> if (reducedOut.isEmpty()) return
    }
    zenityAskIf("Open ReducedOut (type: ${reducedOut::class}) in tmp.notes in IDE ?").exec(cli) || return
    writeFileWithDD(listOf(reducedOut.toString()), tmpNotesFile).exec(cli)
    ideOpen(tmpNotesFile).exec(cli)
}

private val tmpNotesFile = SYS.pathToUserTmp + "/tmp.notes"
