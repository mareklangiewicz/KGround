@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.*

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@OptIn(DelicateKommandApi::class)
fun termGnome(kommand: Kommand? = null, init: TermGnome.() -> Unit = {}) =
    TermGnome().apply {
        kommand?.let { +"--"; nonopts.addAll(kommand.toArgs()) }
        init()
    }

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@DelicateKommandApi
class TermGnome(
    override val opts: MutableList<TermGnomeOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<TermGnomeOpt> {
    override val name get() = "gnome-terminal"
}

@OptIn(DelicateKommandApi::class)
interface TermGnomeOpt: KOptTypical {
    data class Title(val title: String) : KOptL("title", title), TermGnomeOpt
    data object Help : KOptS("h"), TermGnomeOpt
    data object Verbose : KOptS("v"), TermGnomeOpt
}