@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
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
@DelicateApi
data class TermXDefault(
    override val opts: MutableList<KOptTypical> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<KOptTypical> {
    override val name get() = "x-terminal-emulator"
}
