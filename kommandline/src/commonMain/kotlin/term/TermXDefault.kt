@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.kommand.*

/** [debian packages providing x-terminal-emulator](https://packages.debian.org/stable/virtual/x-terminal-emulator) */
@OptIn(DelicateApi::class)
fun termXDefault(kommand: Kommand? = null, init: TermXDefault.() -> Unit = {}) = TermXDefault().apply {
        init()
        kommand?.let { -KOptL(""); nonopts.addAll(kommand.toArgs()) }
            // I assume the "--" separator support. It works at least for gnome-term and kitty,
            // and it clearly separates options from command (and its options) to run.
    }

/** [debian packages providing x-terminal-emulator](https://packages.debian.org/stable/virtual/x-terminal-emulator) */
@DelicateApi("Requires x-terminal-emulator; different terminals accept different options.")
data class TermXDefault(
    override val opts: MutableList<KOptTypical> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<KOptTypical> {
    override val name get() = "x-terminal-emulator"
}


@DelicateApi("API for manual interactive experimentation; requires zenity; can ignore the this kommand.")
fun Kommand.startInTermIfUserConfirms(
    cli: CLI,
    confirmation: String = "Run ::${line()}:: in terminal?",
    title: String = name,
    insideBash: Boolean = true,
    pauseBeforeExit: Boolean = insideBash,
    startInDir: String? = null,
    termKommand: (innerKommand: Kommand) -> Kommand = { termXDefault(it) }
) {
    if (zenityAskIf(confirmation, title).execb(cli)) {
        val k = when {
            insideBash -> bash(this, pauseBeforeExit)
            pauseBeforeExit -> bad { "Can not pause before exit if not using bash shell" }
            else -> this
        }
        cli.start(termKommand(k), dir = startInDir)
    }
}

