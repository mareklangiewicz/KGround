package pl.mareklangiewicz.interactive

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.term.termXDefault
import pl.mareklangiewicz.kommand.ulog
import pl.mareklangiewicz.ulog.d
import pl.mareklangiewicz.ulog.w


// TODO_someday: logging with ulog from context receiver instead of current hacky impl
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
inline fun ifInteractiveCodeEnabled(cli: CLI = SYS, code: () -> Unit) = when {
    !cli.isJvm -> ulog.w("Interactive code is only available on JvmCLI (for now).")
    !getUserFlag(cli, "code.interactive") -> ulog.w("Interactive code NOT enabled.")
    else -> code()
}

@DelicateApi("API for manual interactive experimentation; requires zenity; can ignore the this kommand.")
fun Kommand.tryInteractivelyStartInTerm(
    cli: CLI = SYS,
    confirmation: String = "Start ::${line()}:: in terminal?",
    title: String = name,
    insideBash: Boolean = true,
    pauseBeforeExit: Boolean = insideBash,
    startInDir: String? = null,
    termKommand: (innerKommand: Kommand) -> Kommand = { termXDefault(it) }
) = ifInteractiveCodeEnabled {
    if (zenityAskIf(confirmation, title).axb(cli)) {
        val k = when {
            insideBash -> bash(this, pauseBeforeExit)
            pauseBeforeExit -> bad { "Can not pause before exit if not using bash shell" }
            else -> this
        }
        cli.start(termKommand(k), dir = startInDir)
    }
}


@Suppress("FunctionName")
@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
inline fun <ReducedOut> InteractiveScript(crossinline ax: suspend (cli: CLI) -> ReducedOut) =
    ReducedScript { cli -> ifInteractiveCodeEnabled { ax(cli) } }

// FIXME NOW: I want more InteractiveScripts in Samples instead of "tests" with weird logic when to skip them
//   rethink this
@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
@Deprecated("Better to use Samples with InteractiveScript s")
fun Kommand.tryInteractivelyCheck(expectedLineRaw: String? = null, execInDir: String? = null, cli: CLI = SYS) {
    if (cli.isJvm) toInteractiveCheck(expectedLineRaw, execInDir).axb(cli)
    // ifology just to avoid NotImplementedError on nonjvm. this extension fun will be deleted anyway (execb too)
}

@DelicateApi("API for manual interactive experimentation. Requires Zenity, conditionally skips")
fun Kommand.toInteractiveCheck(expectedLineRaw: String? = null, execInDir: String? = null) =
    InteractiveScript { cli ->
        ulog.d(lineRaw())
        if (expectedLineRaw != null) lineRaw() chkEq expectedLineRaw
        tryInteractivelyStartInTerm(cli, startInDir = execInDir)
    }


@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inLines: List<String>, vararg useNamedArgs: Unit, filePath: String? = null) =
    InteractiveScript { cli ->
        val fp = filePath ?: cli.pathToTmpNotes
        writeFileWithDD(inLines, fp).ax(cli)
        cli.start(gvim(fp))
    }

@DelicateApi("API for manual interactive experimentation. Can ignore all code leaving only some logs.")
fun writeFileAndStartInGVim(inContent: String, vararg useNamedArgs: Unit, filePath: String? = null) =
    writeFileAndStartInGVim(listOf(inContent), filePath = filePath)

