package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.udata.MutLO

fun xdgopen(file: String, init: XdgOpen.() -> Unit = {}) = XdgOpen(file).apply(init)

data class XdgOpen(
  var file: String? = null,
  val options: MutableList<Option> = MutLO(),
) : Kommand {
  override val name get() = "xdg-open"
  override val args get() = options.map { it.str } plusIfNN file

  sealed class Option(val str: String) {
    data object Help : Option("--help") // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
    data object Manual : Option("--manual")
    data object Version : Option("--version") // Don't risk short -v (ambiguity with "verbose" for many commands)
  }

  operator fun Option.unaryMinus() = options.add(this)
}
