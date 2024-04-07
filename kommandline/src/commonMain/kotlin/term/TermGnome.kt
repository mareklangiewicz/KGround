@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.term.TermGnomeOpt.*

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@OptIn(DelicateApi::class)
fun termGnome(kommand: Kommand? = null, init: TermGnome.() -> Unit = {}) =
  TermGnome().apply {
    init()
    kommand?.let { -EOOpt; nonopts.addAll(kommand.toArgs()) }
  }

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@DelicateApi
data class TermGnome(
  override val opts: MutableList<TermGnomeOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<TermGnomeOpt> {
  override val name get() = "gnome-terminal"
}

@OptIn(DelicateApi::class)
interface TermGnomeOpt : KOptTypical {
  data class Title(val title: String) : KOptL("title", title), TermGnomeOpt
  data object Help : KOptS("h"), TermGnomeOpt
  data object Verbose : KOptS("v"), TermGnomeOpt
  data object EOOpt : KOptL(""), TermGnomeOpt
}
