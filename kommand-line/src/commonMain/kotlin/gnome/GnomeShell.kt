@file:Suppress("unused", "ClassName")

package pl.mareklangiewicz.kommand.gnome

import pl.mareklangiewicz.kommand.Kommand
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option
import pl.mareklangiewicz.udata.MutLO
import pl.mareklangiewicz.udata.toMutL

/**[gnome-shell ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-shell.1.html) */
fun gnomeshell(vararg options: Option, init: GnomeShell.() -> Unit = {}) =
  GnomeShell(options.toMutL).apply(init)

/**[gnome-shell ubuntu manpage](http://manpages.ubuntu.com/manpages/impish/man1/gnome-shell.1.html) */
data class GnomeShell(
  val options: MutableList<Option> = MutLO(),
) : Kommand {
  override val name get() = "gnome-shell"
  override val args get() = options.map { it.str }

  sealed class Option(val name: String, val arg: String? = null) {
    val str get() = if (arg == null) name else "$name=$arg"

    data object Nested : Option("--nested")
    data object Wayland : Option("--wayland")
    data object Replace : Option("--replace")
    data object SmDisable : Option("--sm-disable")
    data class SmClientId(val id: String) : Option("--sm-client-id", id)
    data class SmSaveFile(val file: String) : Option("--sm-save-file", file)
    data class Screen(val s: String) : Option("--screen", s)
    data class Display(val d: String) : Option("--display", d)
    data object Sync : Option("--sync")
    data object Version : Option("--version") // Don't risk short -v (ambiguity with "verbose" for many commands)
    data object Help : Option("--help") // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
    data class Mode(val m: String) : Option("--mode", m)
    data object ListModes : Option("--listmodes")
    data class ClutterDisplay(val d: String) : Option("--clutter-display", d)
  }

  operator fun Option.unaryMinus() = options.add(this)
}
