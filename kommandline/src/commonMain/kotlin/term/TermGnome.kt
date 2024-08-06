@file:Suppress("ClassName")

package pl.mareklangiewicz.kommand.term

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.term.TermGnomeOpt.*

@OptIn(DelicateApi::class)
fun Kommand.inTermGnome(): TermGnome = termGnome(this)

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@OptIn(DelicateApi::class)
fun termGnome(kommand: Kommand? = null, init: TermGnome.() -> Unit = {}): TermGnome =
  TermGnome().apply {
    init()
    kommand?.let { -EOOpt; nonopts.addAll(kommand.toArgs()) }
  }

/** [gnome-terminal ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-terminal.1.html) */
@DelicateApi
data class TermGnome(
  override val opts: MutableList<TermGnomeOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<TermGnomeOpt>, TermKommand {
  override val name get() = "gnome-terminal"
}

@OptIn(DelicateApi::class)
interface TermGnomeOpt : KOptTypical {

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), TermGnomeOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), TermGnomeOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), TermGnomeOpt
  // endregion [GNU Common Opts]

  data class Title(val title: String) : KOptL("title", title), TermGnomeOpt
  data object Verbose : KOptS("v"), TermGnomeOpt
}
